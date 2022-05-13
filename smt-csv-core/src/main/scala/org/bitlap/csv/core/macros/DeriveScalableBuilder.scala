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

import java.io.InputStream
import scala.collection.mutable
import scala.reflect.macros.whitebox

/**
 * @author 梦境迷离
 * @version 1.0,2022/4/29
 */
class DeriveScalableBuilder(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

  import c.universe._

  private val annoBuilderPrefix = "_AnonScalableBuilder$"

  private val builderFunctionPrefix = "_ScalableBuilderFunction$"

  private val innerColumnFuncTermName = TermName("_columns")
  private val innerLName = q"_l"
  private val innerTempTermName = TermName("_line")
  private val scalableInstanceTermName = TermName("_scalableInstance")
  private val scalableImplClassNamePrefix = "_ScalaAnno$"

  // scalafmt: { maxColumn = 400 }
  def setFieldImpl[T: WeakTypeTag, SF: WeakTypeTag](scalaField: Expr[T => SF], value: Expr[String => SF]): Expr[ScalableBuilder[T]] = {
    val Function(_, Select(_, termName)) = scalaField.tree
    val builderId = getBuilderId(annoBuilderPrefix)
    MacroCache.builderFunctionTrees.getOrElseUpdate(builderId, mutable.Map.empty).update(termName.toString, value)
    val tree = q"new ${c.prefix.actualType}"
    exprPrintTree[ScalableBuilder[T]](force = false, tree)
  }

  def applyImpl[T: WeakTypeTag]: Expr[ScalableBuilder[T]] =
    deriveBuilderApplyImpl[T]

  def buildImpl[T: WeakTypeTag](line: Expr[String], columnSeparator: Expr[Char]): Expr[Scalable[T]] = {
    val clazzName = resolveClazzTypeName[T]
    deriveScalableImpl[T](clazzName, line, columnSeparator)
  }

  def convertImpl[T: WeakTypeTag](lines: Expr[List[String]], columnSeparator: Expr[Char]): Expr[List[Option[T]]] = {
    val clazzName = resolveClazzTypeName[T]
    deriveFullScalableImpl[T](clazzName, lines, columnSeparator)
  }

  def convertDefaultImpl[T: WeakTypeTag](lines: Expr[List[String]]): Expr[List[Option[T]]] = {
    val clazzName = resolveClazzTypeName[T]
    deriveFullScalableImpl[T](clazzName, lines, c.Expr[Char](q"','"))
  }

  def buildDefaultImpl[T: WeakTypeTag](line: Expr[String]): Expr[Scalable[T]] = {
    val clazzName = resolveClazzTypeName[T]
    deriveScalableImpl[T](clazzName, line, c.Expr[Char](q"','"))
  }

  def readFromFileImpl[T: WeakTypeTag](file: Expr[InputStream], charset: Expr[String]): Expr[List[Option[T]]] = {
    val clazzName = resolveClazzTypeName[T]
    deriveFullFromFileScalableImpl[T](clazzName, file, charset, c.Expr[Char](q"','"))
  }

  private def deriveBuilderApplyImpl[T: WeakTypeTag]: Expr[ScalableBuilder[T]] = {
    val className = TypeName(annoBuilderPrefix + MacroCache.getBuilderId)
    val caseClazzName = TypeName(weakTypeOf[T].typeSymbol.name.decodedName.toString)
    val tree =
      q"""
        class $className extends $packageName.ScalableBuilder[$caseClazzName]
        new $className
       """
    exprPrintTree[ScalableBuilder[T]](force = false, tree)
  }

  private def getPreTree: Iterable[Tree] = {
    val customTrees = MacroCache.builderFunctionTrees.getOrElse(getBuilderId(annoBuilderPrefix), mutable.Map.empty)
    val (_, preTrees) = customTrees.collect { case (key, expr: Expr[Tree] @unchecked) =>
      expr.tree match {
        case buildFunction: Function =>
          val functionName = TermName(builderFunctionPrefix + key)
          key -> q"lazy val $functionName: ${buildFunction.tpe} = $buildFunction"
      }
    }.unzip
    preTrees
  }

  // scalafmt: { maxColumn = 400 }
  private def deriveFullFromFileScalableImpl[T: WeakTypeTag](clazzName: TypeName, file: Expr[InputStream], charset: Expr[String], columnSeparator: Expr[Char]): Expr[List[Option[T]]] = {
    // NOTE: preTrees must be at the same level as Scalable
    val tree =
      q"""
         ..$getPreTree
         ..${getAnnoClassObject[T](clazzName, columnSeparator)}
         $packageName.FileUtils.reader($file, $charset).map { ($innerLName: String) =>
             $scalableInstanceTermName.$innerTempTermName = ${TermName(innerLName.toString())}
             $scalableInstanceTermName.toScala 
         }
      """
    exprPrintTree[List[Option[T]]](force = false, tree)
  }

  // scalafmt: { maxColumn = 400 }
  private def deriveFullScalableImpl[T: WeakTypeTag](clazzName: TypeName, lines: Expr[List[String]], columnSeparator: Expr[Char]): Expr[List[Option[T]]] = {
    // NOTE: preTrees must be at the same level as Scalable
    val tree =
      q"""
         ..$getPreTree
         ..${getAnnoClassObject[T](clazzName, columnSeparator)}
         $lines.map { ($innerLName: String) =>
             $scalableInstanceTermName.$innerTempTermName = ${TermName(innerLName.toString())}
             $scalableInstanceTermName.toScala 
         }
      """
    exprPrintTree[List[Option[T]]](force = false, tree)
  }

  private def getAnnoClassObject[T: WeakTypeTag](clazzName: TypeName, columnSeparator: Expr[Char]): Tree = {
    val annoClassName = TermName(scalableImplClassNamePrefix + MacroCache.getIdentityId)
    q"""
       object $annoClassName extends $packageName.Scalable[$clazzName] {
           var $innerTempTermName: String = _
           private val $innerColumnFuncTermName = () => $packageName.StringUtils.splitColumns(${annoClassName.toTermName}.$innerTempTermName, $columnSeparator)
            ..${scalableBody[T](clazzName, innerColumnFuncTermName)}
       }
       private final lazy val $scalableInstanceTermName = $annoClassName
     """
  }

  // scalafmt: { maxColumn = 400 }
  private def deriveScalableImpl[T: WeakTypeTag](clazzName: TypeName, line: Expr[String], columnSeparator: Expr[Char]): Expr[Scalable[T]] = {
    val annoClassName = TermName(scalableImplClassNamePrefix + MacroCache.getIdentityId)
    // NOTE: preTrees must be at the same level as Scalable
    val tree =
      q"""
         ..$getPreTree
         object $annoClassName extends $packageName.Scalable[$clazzName] {
            final lazy private val $innerColumnFuncTermName = () => $packageName.StringUtils.splitColumns($line, $columnSeparator)
            ..${scalableBody[T](clazzName, innerColumnFuncTermName)}
         }
         $annoClassName
      """
    exprPrintTree[Scalable[T]](force = false, tree)
  }

  // scalafmt: { maxColumn = 400 }
  private def scalableBody[T: WeakTypeTag](clazzName: TypeName, innerFuncTermName: TermName): Tree = {
    val customTrees = MacroCache.builderFunctionTrees.getOrElse(getBuilderId(annoBuilderPrefix), mutable.Map.empty)
    val params = getCaseClassParams[T]()
    val fieldNames = params.map(_.name.decodedName.toString)
    val fields = checkCaseClassZipAll[T](innerFuncTermName).map { idxType =>
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
