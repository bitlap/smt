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

import org.bitlap.csv.core.{ Scalable, ScalableBuilder }

import scala.collection.mutable
import scala.reflect.macros.whitebox

/**
 * @author 梦境迷离
 * @version 1.0,2022/4/29
 */
class DeriveScalableBuilder(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

  import c.universe._

  private val packageName = q"_root_.org.bitlap.csv.core"

  private val annoBuilderPrefix = "_AnonScalableBuilder$"
  private val builderFunctionPrefix = "_ScalableBuilderFunction$"

  def setFieldImpl[T: c.WeakTypeTag, SF: c.WeakTypeTag](
    scalaField: c.Expr[T ⇒ SF],
    value: c.Expr[String ⇒ SF]
  ): c.Expr[ScalableBuilder[T]] = {
    val Function(_, Select(_, termName)) = scalaField.tree
    val builderId = getBuilderId(annoBuilderPrefix)
    MacroCache.builderFunctionTrees.getOrElseUpdate(builderId, mutable.Map.empty).update(termName.toString, value)
    val tree = q"new ${c.prefix.actualType}"
    printTree[ScalableBuilder[T]](force = true, tree)
  }

  def applyImpl[T: c.WeakTypeTag]: c.Expr[ScalableBuilder[T]] =
    deriveBuilderApplyImpl[T]

  def buildImpl[T: c.WeakTypeTag](line: c.Expr[String], columnSeparator: c.Expr[Char]): c.Expr[Scalable[T]] =
    deriveScalableImpl[T](line, columnSeparator)

  def buildDefaultImpl[T: c.WeakTypeTag](line: c.Expr[String]): c.Expr[Scalable[T]] =
    deriveScalableImpl[T](line, c.Expr[Char](q"','"))

  private def deriveBuilderApplyImpl[T: WeakTypeTag]: c.Expr[ScalableBuilder[T]] = {
    val className = TypeName(annoBuilderPrefix + MacroCache.getBuilderId)
    val caseClazzName = TypeName(c.weakTypeOf[T].typeSymbol.name.decodedName.toString)
    val tree =
      q"""
        class $className extends $packageName.ScalableBuilder[$caseClazzName]
        new $className
       """
    printTree[ScalableBuilder[T]](force = true, tree)

  }

  private def deriveScalableImpl[T: c.WeakTypeTag](
    line: c.Expr[String],
    columnSeparator: c.Expr[Char]
  ): c.Expr[Scalable[T]] = {
    val clazzName = TypeName(c.weakTypeOf[T].typeSymbol.name.decodedName.toString)
    val customTrees = MacroCache.builderFunctionTrees.getOrElse(getBuilderId(annoBuilderPrefix), mutable.Map.empty)
    val (_, preTrees) = customTrees.collect { case (key, expr: Expr[Tree] @unchecked) ⇒
      expr.tree match {
        case buildFunction: Function ⇒
          val functionName = TermName(builderFunctionPrefix + key)
          key -> q"lazy  val $functionName: ${buildFunction.tpe} = $buildFunction"
      }
    }.unzip
    val innerVarTermName = TermName("_columns")
    // NOTE: preTrees must be at the same level as Scalable
    val tree =
      q"""
         ..$preTrees
         new $packageName.Scalable[$clazzName] {
            final lazy private val $innerVarTermName = _root_.org.bitlap.csv.core.StringUtils.splitColumns($line, $columnSeparator)
            ..${scalableBody[T](clazzName, innerVarTermName)}
         }
      """
    printTree[Scalable[T]](force = true, tree)
  }

  private def scalableBody[T: c.WeakTypeTag](
    clazzName: TypeName,
    innerVarTermName: TermName
  ): Tree = {
    val customTrees = MacroCache.builderFunctionTrees.getOrElse(getBuilderId(annoBuilderPrefix), mutable.Map.empty)
    val params = getCaseClassParams[T]()
    val fieldNames = params.map(_.name.decodedName.toString)
    val fields = checkCaseClassZipParams[T](innerVarTermName).map { idxType =>
      val idx = idxType._1._1
      val columnValues = idxType._1._2
      val fieldTypeName = TypeName(idxType._2.typeSymbol.name.decodedName.toString)
      val customFunction = () => q"${TermName(builderFunctionPrefix + fieldNames(idx))}.apply($columnValues)"
      idxType._2 match {
        case tp if tp <:< typeOf[List[_]] =>
          val genericType = c.typecheck(q"${idxType._2}", c.TYPEmode).tpe.typeArgs.head
          if (customTrees.contains(fieldNames(idx))) {
            q"${customFunction()}.asInstanceOf[List[$genericType]]"
          } else {
            c.abort(
              c.enclosingPosition,
              s"Missing usage `setField` for parsing `$clazzName.${fieldNames(idx)}` as a `List` , you have to define a custom way by using `setField` method!"
            )
            // q"$packageName.Scalable[${genericType.typeSymbol.name.toTypeName}]._toScala($columnValues)"
          }
        case tp if tp <:< typeOf[Seq[_]] =>
          val genericType = c.typecheck(q"${idxType._2}", c.TYPEmode).tpe.typeArgs.head
          if (customTrees.contains(fieldNames(idx))) {
            q"${customFunction()}.asInstanceOf[Seq[$genericType]]"
          } else {
            c.abort(
              c.enclosingPosition,
              s"Missing usage `setField` for parsing `$clazzName.${fieldNames(idx)}` as a `Seq` , you have to define a custom way by using `setField` method!"
            )
            // q"$packageName.Scalable[${genericType.typeSymbol.name.toTypeName}]._toScala($columnValues)"
          }
        case tp if tp <:< typeOf[Option[_]] =>
          val genericType = c.typecheck(q"${idxType._2}", c.TYPEmode).tpe.typeArgs.head
          if (customTrees.contains(fieldNames(idx))) {
            q"${customFunction()}.asInstanceOf[Option[$genericType]]"
          } else {
            q"$packageName.Scalable[${genericType.typeSymbol.name.toTypeName}]._toScala($columnValues)"
          }
        case _ =>
          if (customTrees.contains(fieldNames(idx))) {
            q"${customFunction()}.asInstanceOf[$fieldTypeName]"
          } else {
            idxType._2 match {
              case t if t =:= typeOf[Int] =>
                q"$packageName.Scalable[$fieldTypeName]._toScala($columnValues).getOrElse(0)"
              case t if t =:= typeOf[String] =>
                q"""$packageName.Scalable[$fieldTypeName]._toScala($columnValues).getOrElse("")"""
              case t if t =:= typeOf[Float] =>
                q"$packageName.Scalable[$fieldTypeName]._toScala($columnValues).getOrElse[Float](0.asInstanceOf[Float])"
              case t if t =:= typeOf[Double] =>
                q"$packageName.Scalable[$fieldTypeName]._toScala($columnValues).getOrElse[Double](0D)"
              case t if t =:= typeOf[Char] =>
                q"$packageName.Scalable[$fieldTypeName]._toScala($columnValues).getOrElse('?')"
              case t if t =:= typeOf[Byte] =>
                q"$packageName.Scalable[$fieldTypeName]._toScala($columnValues).getOrElse(0)"
              case t if t =:= typeOf[Short] =>
                q"$packageName.Scalable[$fieldTypeName]._toScala($columnValues).getOrElse(0)"
              case t if t =:= typeOf[Boolean] =>
                q"$packageName.Scalable[$fieldTypeName]._toScala($columnValues).getOrElse(false)"
              case t if t =:= typeOf[Long] =>
                q"$packageName.Scalable[$fieldTypeName]._toScala($columnValues).getOrElse(0L)"
            }
          }
      }
    }

    q"override def toScala: Option[$clazzName] = Option(${TermName(clazzName.decodedName.toString)}(..$fields))"
  }
}
