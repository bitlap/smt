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

import org.bitlap.csv.core.{ Csvable, CsvableBuilder }

import scala.collection.mutable
import scala.reflect.macros.whitebox

/**
 * @author 梦境迷离
 * @version 1.0,2022/4/29
 */
class DeriveCsvableBuilder(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

  import c.universe._

  private val packageName = q"_root_.org.bitlap.csv.core"

  private val annoBuilderPrefix = "_AnonCsvableBuilder$"

  private val builderFunctionPrefix = "_CsvableBuilderFunction$"

  def setFieldImpl[T: c.WeakTypeTag, SF: c.WeakTypeTag](
    scalaField: c.Expr[T ⇒ SF],
    value: c.Expr[SF => String]
  ): c.Expr[CsvableBuilder[T]] = {
    val Function(_, Select(_, termName)) = scalaField.tree
    val builderId = getBuilderId(annoBuilderPrefix)
    MacroCache.builderFunctionTrees.getOrElseUpdate(builderId, mutable.Map.empty).update(termName.toString, value)
    val tree = q"new ${c.prefix.actualType}"
    printTree[CsvableBuilder[T]](force = true, tree)
  }

  def applyImpl[T: c.WeakTypeTag]: c.Expr[CsvableBuilder[T]] =
    deriveBuilderApplyImpl[T]

  def buildImpl[T: c.WeakTypeTag](t: c.Expr[T], columnSeparator: c.Expr[Char]): c.Expr[Csvable[T]] =
    deriveCsvableImpl[T](t, columnSeparator)

  private def deriveBuilderApplyImpl[T: WeakTypeTag]: c.Expr[CsvableBuilder[T]] = {
    val className = TypeName(annoBuilderPrefix + MacroCache.getBuilderId)
    val caseClazzName = TypeName(c.weakTypeOf[T].typeSymbol.name.decodedName.toString)
    val tree =
      q"""
        class $className extends $packageName.CsvableBuilder[$caseClazzName]
        new $className
      """
    printTree[CsvableBuilder[T]](force = true, tree)
  }

  private def deriveCsvableImpl[T: c.WeakTypeTag](t: c.Expr[T], columnSeparator: c.Expr[Char]): c.Expr[Csvable[T]] = {
    val clazzName = resolveClazzTypeName[T]
    val customTrees = MacroCache.builderFunctionTrees.getOrElse(getBuilderId(annoBuilderPrefix), mutable.Map.empty)
    val (_, preTrees) = customTrees.collect { case (key, expr: Expr[Tree] @unchecked) ⇒
      expr.tree match {
        case buildFunction: Function ⇒
          val functionName = TermName(builderFunctionPrefix + key)
          key -> q"lazy val $functionName: ${c.typecheck(q"${buildFunction.tpe}", c.TYPEmode).tpe} = $buildFunction"
      }
    }.unzip
    val innerVarTermName = TermName("_t")
    val tree =
      q"""
         ..$preTrees
         new $packageName.Csvable[$clazzName] {
            lazy val $innerVarTermName = $t
            ..${CsvableBody[T](columnSeparator, innerVarTermName, customTrees)}
         }
      """
    printTree[Csvable[T]](force = false, tree)
  }

  private def CsvableBody[T: c.WeakTypeTag](
    columnSeparator: c.Expr[Char],
    innerVarTermName: TermName,
    customTrees: mutable.Map[String, Any]
  ): c.Expr[Csvable[T]] = {
    val clazzName = resolveClazzTypeName[T]
    val (fieldNames, indexTypes) = zipAllCaseClassParams
    val indexByName = (i: Int) => TermName(fieldNames(i))
    val fieldsToString = indexTypes.map { idxType =>
      val customFunction = () =>
        q"${TermName(builderFunctionPrefix + fieldNames(idxType._1))}.apply($innerVarTermName.${indexByName(idxType._1)})"
      if (idxType._2 <:< typeOf[Option[_]]) {
        val genericType = c.typecheck(q"${idxType._2}", c.TYPEmode).tpe.typeArgs.head
        if (customTrees.contains(fieldNames(idxType._1))) {
          customFunction()
        } else {
          q"""
              $packageName.Csvable[${genericType.typeSymbol.name.toTypeName}]._toCsvString {
                if ($innerVarTermName.${indexByName(idxType._1)}.isEmpty) "" else $innerVarTermName.${indexByName(
            idxType._1
          )}.get
              }
            """
        }
      } else {
        if (customTrees.contains(fieldNames(idxType._1))) {
          customFunction()
        } else {
          q"$packageName.Csvable[${TypeName(idxType._2.typeSymbol.name.decodedName.toString)}]._toCsvString($innerVarTermName.${indexByName(idxType._1)})"
        }
      }
    }
    val separator = q"$columnSeparator"
    val tree =
      q"""
         override def toCsvString: String = {
            val fields = ${clazzName.toTermName}.unapply(_t).orNull
            if (null == fields) "" else $fieldsToString.mkString($separator.toString)
         }
      """
    printTree[Csvable[T]](force = false, tree)
  }

}
