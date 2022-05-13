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

  private val annoBuilderPrefix = "_AnonCsvableBuilder$"

  private val builderFunctionPrefix = "_CsvableBuilderFunction$"

  def setFieldImpl[T: WeakTypeTag, SF: WeakTypeTag](
    scalaField: Expr[T => SF],
    value: Expr[SF => String]
  ): Expr[CsvableBuilder[T]] = {
    val Function(_, Select(_, termName)) = scalaField.tree
    val builderId = getBuilderId(annoBuilderPrefix)
    MacroCache.builderFunctionTrees.getOrElseUpdate(builderId, mutable.Map.empty).update(termName.toString, value)
    val tree = q"new ${c.prefix.actualType}"
    exprPrintTree[CsvableBuilder[T]](force = false, tree)
  }

  def applyImpl[T: WeakTypeTag]: Expr[CsvableBuilder[T]] =
    deriveBuilderApplyImpl[T]

  def buildDefaultImpl[T: WeakTypeTag](t: Expr[T]): Expr[Csvable[T]] =
    deriveCsvableImpl[T](t, c.Expr[Char](q"','"))

  def buildImpl[T: WeakTypeTag](t: Expr[T], columnSeparator: Expr[Char]): Expr[Csvable[T]] =
    deriveCsvableImpl[T](t, columnSeparator)

  def convertImpl[T: WeakTypeTag](ts: Expr[List[T]], columnSeparator: Expr[Char]): Expr[String] =
    deriveFullCsvableImpl[T](ts, columnSeparator)

  def convertDefaultImpl[T: WeakTypeTag](ts: Expr[List[T]]): Expr[String] =
    deriveFullCsvableImpl[T](ts, c.Expr[Char](q"','"))

  private def deriveBuilderApplyImpl[T: WeakTypeTag]: Expr[CsvableBuilder[T]] = {
    val className = TypeName(annoBuilderPrefix + MacroCache.getBuilderId)
    val caseClazzName = TypeName(weakTypeOf[T].typeSymbol.name.decodedName.toString)
    val tree =
      q"""
        class $className extends $packageName.CsvableBuilder[$caseClazzName]
        new $className
      """
    exprPrintTree[CsvableBuilder[T]](force = false, tree)
  }

  private def getCustomPreTress: (mutable.Map[String, Any], Iterable[Tree]) = {
    val customTrees = MacroCache.builderFunctionTrees.getOrElse(getBuilderId(annoBuilderPrefix), mutable.Map.empty)
    val (_, preTrees) = customTrees.collect { case (key, expr: Expr[Tree] @unchecked) =>
      expr.tree match {
        case buildFunction: Function =>
          val functionName = TermName(builderFunctionPrefix + key)
          key -> q"lazy val $functionName: ${c.typecheck(q"${buildFunction.tpe}", c.TYPEmode).tpe} = $buildFunction"
      }
    }.unzip
    customTrees -> preTrees
  }

  private def deriveFullCsvableImpl[T: WeakTypeTag](
    ts: Expr[List[T]],
    columnSeparator: Expr[Char]
  ): Expr[String] = {
    val clazzName = resolveClazzTypeName[T]
    val (customTrees, preTrees) = getCustomPreTress
    val innerTName = q"_t"
    val tree =
      q"""
         ..$preTrees
         $ts.map { ($innerTName: $clazzName) =>
             new $packageName.Csvable[$clazzName] {
                ..${CsvableBody[T](columnSeparator, TermName(innerTName.toString()), customTrees)}
             }.toCsvString
         }.mkString("\n")
      """
    exprPrintTree[String](force = false, tree)
  }

  private def deriveCsvableImpl[T: WeakTypeTag](t: Expr[T], columnSeparator: Expr[Char]): Expr[Csvable[T]] = {
    val clazzName = resolveClazzTypeName[T]
    val (customTrees, preTrees) = getCustomPreTress
    val innerVarTermName = TermName("_t")
    val tree =
      q"""
         ..$preTrees
         new $packageName.Csvable[$clazzName] {
            private final val $innerVarTermName = $t
            ..${CsvableBody[T](columnSeparator, innerVarTermName, customTrees)}
         }
      """
    exprPrintTree[Csvable[T]](force = false, tree)
  }

  private def CsvableBody[T: WeakTypeTag](
    columnSeparator: Expr[Char],
    innerVarTermName: TermName,
    customTrees: mutable.Map[String, Any]
  ): Expr[Csvable[T]] = {
    val clazzName = resolveClazzTypeName[T]
    val (fieldNames, indexTypes) = checkCaseClassZip
    val indexByName = (i: Int) => TermName(fieldNames(i))
    val fieldsToString = indexTypes.map { idxType =>
      val customFunction = () =>
        q"${TermName(builderFunctionPrefix + fieldNames(idxType._1))}.apply($innerVarTermName.${indexByName(idxType._1)})"
      idxType._2 match {
        case t if t <:< typeOf[List[_]] =>
          if (customTrees.contains(fieldNames(idxType._1))) {
            q"${customFunction()}"
          } else {
            c.abort(
              c.enclosingPosition,
              s"Missing usage `setField` for converting `$clazzName.${fieldNames(idxType._1)}` as a `String` , you have to define a custom way by using `setField` method!"
            )
          }
        case t if t <:< typeOf[Seq[_]] =>
          if (customTrees.contains(fieldNames(idxType._1))) {
            q"${customFunction()}"
          } else {
            c.abort(
              c.enclosingPosition,
              s"Missing usage `setField` for converting `$clazzName.${fieldNames(idxType._1)}` as a `String` , you have to define a custom way by using `setField` method!"
            )
          }
        case t if t <:< typeOf[Option[_]] =>
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
        case _ =>
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
    exprPrintTree[Csvable[T]](force = false, tree)
  }

}
