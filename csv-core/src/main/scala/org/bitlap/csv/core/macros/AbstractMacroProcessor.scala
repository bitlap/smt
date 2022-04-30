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

/**
 *
 * @author 梦境迷离
 * @since 2021/7/24
 * @version 1.0
 */
abstract class AbstractMacroProcessor(val c: blackbox.Context) {

  import c.universe._

  def checkCaseClassReturnConstructorParams[T: c.WeakTypeTag](
    line:            c.Expr[String],
    columnSeparator: c.Expr[Char]
  ): List[(Tree, Type)] = {
    lazy val columns = q"_root_.org.bitlap.csv.core.StringUtils.splitColumns($line, $columnSeparator)"
    val idxColumn = (i: Int) => q"$columns($i)"
    val params = getCaseClassParams[T]()
    val paramsSize = params.size
    val types = params.map(f => c.typecheck(tq"$f", c.TYPEmode).tpe)
    val indexColumns = (0 until paramsSize).toList.map(i => idxColumn(i))
    if (indexColumns.size != types.size) {
      c.abort(c.enclosingPosition, "The column num of CSV file is different from that in case class constructor!")
    }

    indexColumns zip types
  }

  def getCaseClassParams[T: c.WeakTypeTag](): List[Symbol] = {
    val parameters = c.weakTypeOf[T].resultType.member(TermName("<init>")).typeSignature.paramLists
    if (parameters.size > 1) {
      c.abort(c.enclosingPosition, "The constructor of case class has currying!")
    }
    parameters.flatten
  }

  def printTree[T: c.WeakTypeTag](force: Boolean, resTree: c.Tree): c.Expr[T] = {
    c.info(
      c.enclosingPosition,
      s"\n###### Time: ${
        ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
      } " +
        s"Expanded macro start ######\n" + resTree.toString() + "\n###### Expanded macro end ######\n",
      force = force
    )
    c.Expr[T](resTree)
  }

  def stringMacroImpl[T: c.WeakTypeTag](t: c.Expr[T], columnSeparator: c.Expr[Char], termName: TermName): c.Expr[String] = {
    val clazzName = c.weakTypeOf[T].typeSymbol.name
    val parameters = c.weakTypeOf[T].resultType.member(TermName("<init>")).typeSignature.paramLists
    if (parameters.size > 1) {
      c.abort(c.enclosingPosition, "The constructor of case class has currying!")
    }
    val params = parameters.flatten
    val paramsSize = params.size
    val names = params.map(p => p.name.decodedName.toString)
    val indexByName = (i: Int) => TermName(names(i))
    val indexTypes = params.zip(0 until paramsSize).map(f => f._2 -> c.typecheck(tq"${f._1}", c.TYPEmode).tpe)
    val fieldsToString = indexTypes.map {
      idxType =>
        if (idxType._2 <:< typeOf[Option[_]]) {
          val genericType = c.typecheck(q"${idxType._2}", c.TYPEmode).tpe.typeArgs.head
          q"""$termName[${genericType.typeSymbol.name.toTypeName}].toCsvString { 
                  if (${TermName("t")}.${indexByName(idxType._1)}.isEmpty) "" else ${TermName("t")}.${indexByName(idxType._1)}.get
                }"""
        } else {
          q"$termName[${TypeName(idxType._2.typeSymbol.name.decodedName.toString)}].toCsvString(${TermName("t")}.${indexByName(idxType._1)})"
        }
    }
    val separator = q"$columnSeparator"
    val tree =
      q"""
        val fields = ${TermName(clazzName.decodedName.toString)}.unapply($t).orNull
        if (null == fields) "" else $fieldsToString.mkString($separator.toString)
       """

    printTree[String](force = true, tree)
  }
}
