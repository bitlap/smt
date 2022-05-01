/*
 * Copyright (c) 2022 bitlap
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.bitlap.csv.core.macros

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import scala.reflect.macros.blackbox
import scala.collection.mutable

/**
 * @author 梦境迷离
 * @since 2021/7/24
 * @version 1.0
 */
abstract class AbstractMacroProcessor(val c: blackbox.Context) {

  import c.universe._

  private[macros] def checkCaseClassZipParams[T: c.WeakTypeTag](
    columns: TermName
  ): List[((Int, Tree), Type)] = {
    val idxColumn = (i: Int) => q"${columns}(${i})"
    val params = getCaseClassParams[T]()
    val paramsSize = params.size
    val types = params.map(f => c.typecheck(tq"$f", c.TYPEmode).tpe)
    val indexColumns = (0 until paramsSize).toList.map(i => i -> idxColumn(i))
    if (indexColumns.size != types.size) {
      c.abort(c.enclosingPosition, "The column num of CSV file is different from that in case class constructor!")
    }

    indexColumns zip types
  }

  private[macros] def getCaseClassParams[T: c.WeakTypeTag](): List[Symbol] = {
    val parameters = resolveParameters[T]
    if (parameters.size > 1) {
      c.abort(c.enclosingPosition, "The constructor of case class has currying!")
    }
    parameters.flatten
  }

  def printTree[T: c.WeakTypeTag](force: Boolean, resTree: c.Tree): c.Expr[T] = {
    c.info(
      c.enclosingPosition,
      s"\n###### Time: ${ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)} Expanded macro start ######\n" + resTree
        .toString() + "\n###### Expanded macro end ######\n",
      force = force
    )
    c.Expr[T](resTree)
  }

  private[macros] def getClassInstance[T: c.WeakTypeTag](className: TypeName, superClass: TypeName): c.universe.Tree = {
    val caseClazzName = TypeName(c.weakTypeOf[T].typeSymbol.name.decodedName.toString)
    c.weakTypeOf[T] match {
      case t if t <:< typeOf[List[_]] =>
        val genericType = c.typecheck(q"${c.weakTypeOf[T]}", c.TYPEmode).tpe.typeArgs.head
        q"""
          class $className extends $superClass[List[$genericType]]
          new $className
        """
      case t if t <:< typeOf[mutable.Seq[_]] =>
        val genericType = c.typecheck(q"${c.weakTypeOf[T]}", c.TYPEmode).tpe.typeArgs.head
        q"""
          class $className extends $superClass[Seq[$genericType]]
          new $className  
        """
      case _ =>
        q"""
          class $className extends $superClass[$caseClazzName]
          new $className
        """
    }
  }

  private[macros] def resolveParameters[T: c.WeakTypeTag]: List[List[Symbol]] =
    c.weakTypeOf[T].resultType.member(TermName("<init>")).typeSignature.paramLists

  private[macros] def resolveClazzTypeName[T: c.WeakTypeTag]: c.universe.TypeName =
    TypeName(c.weakTypeOf[T].typeSymbol.name.decodedName.toString)

  private[macros] def zipAllCaseClassParams[T: c.WeakTypeTag]: (List[String], List[(Int, Type)]) = {
    val parameters = resolveParameters[T]
    if (parameters.size > 1) {
      c.abort(c.enclosingPosition, "The constructor of case class has currying!")
    }
    val params = parameters.flatten
    val paramsSize = params.size
    val names = params.map(p => p.name.decodedName.toString)
    names -> params.zip(0 until paramsSize).map(f => f._2 -> c.typecheck(tq"${f._1}", c.TYPEmode).tpe)
  }

  private[macros] def getBuilderId(annoBuilderPrefix: String): Int =
    c.prefix.actualType.toString.replace(annoBuilderPrefix, "").toInt
}
