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

package org.bitlap.csv.macros

import org.bitlap.common.{ AbstractMacroProcessor, MacroCache }
import org.bitlap.csv.{ CsvFormat, CsvableBuilder }

import java.io.File
import scala.collection.mutable
import scala.reflect.macros.whitebox

/** @author
 *    梦境迷离
 *  @version 1.0,2022/4/29
 */
class DeriveCsvableBuilder(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

  import c.universe._
  protected val packageName = q"_root_.org.bitlap.csv"

  private val annoBuilderPrefix = "_AnonCsvableBuilder$"

  private val builderFunctionPrefix = "_CsvableBuilderFunction$"

  private val innerTName                 = q"_t"
  private val innerTmpTermName           = TermName("_tt")
  private val csvableInstanceTermName    = TermName("_csvableInstance")
  private val csvableImplClassNamePrefix = "_CsvAnno$"
  private val funcArgsTempTermName       = TermName("temp")

  // scalafmt: { maxColumn = 400 }
  def setFieldImpl[T: WeakTypeTag, SF: WeakTypeTag](scalaField: Expr[T => SF], value: Expr[SF => String]): Expr[CsvableBuilder[T]] = {
    val Function(_, Select(_, termName)) = scalaField.tree
    val builderId                        = getBuilderId(annoBuilderPrefix)
    MacroCache.builderFunctionTrees.getOrElseUpdate(builderId, mutable.Map.empty).update(termName.toString, value)
    val tree = q"new ${c.prefix.actualType}"
    exprPrintTree[CsvableBuilder[T]](force = false, tree)
  }

  def applyImpl[T: WeakTypeTag]: Expr[CsvableBuilder[T]] =
    deriveBuilderApplyImpl[T]

  def convertOneImpl[T: WeakTypeTag](t: Expr[T])(format: c.Expr[CsvFormat]): Expr[String] =
    deriveCsvableImpl[T](t, format)

  def convertAllImpl[T: WeakTypeTag](ts: Expr[List[T]])(format: c.Expr[CsvFormat]): Expr[String] =
    deriveFullCsvableImpl[T](ts, format)

  def convertToFileImpl[T: WeakTypeTag](ts: Expr[List[T]], file: Expr[File])(format: c.Expr[CsvFormat]): Expr[Boolean] =
    deriveFullIntoFileCsvableImpl[T](ts, file, format)

  private def deriveBuilderApplyImpl[T: WeakTypeTag]: Expr[CsvableBuilder[T]] = {
    val className     = TypeName(annoBuilderPrefix + MacroCache.getBuilderId)
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

  // scalafmt: { maxColumn = 400 }
  private def deriveFullIntoFileCsvableImpl[T: WeakTypeTag](ts: Expr[List[T]], file: Expr[File], format: c.Expr[CsvFormat]): Expr[Boolean] = {
    val clazzName               = resolveClassTypeName[T]
    val (customTrees, preTrees) = getCustomPreTress
    val tree =
      q"""
         ..$preTrees
         ..${getAnnoClassObject[T](customTrees, format)}
         $packageName.FileUtils.writer($file, $ts.map { ($innerTName: $clazzName) =>
               $csvableInstanceTermName.$innerTmpTermName = $innerTName
               $csvableInstanceTermName._toCsvString($innerTName)
           }, $format
         )
      """
    exprPrintTree[Boolean](force = false, tree)
  }

  // scalafmt: { maxColumn = 400 }
  private def deriveFullCsvableImpl[T: WeakTypeTag](ts: Expr[List[T]], format: c.Expr[CsvFormat]): Expr[String] = {
    val clazzName               = resolveClassTypeName[T]
    val (customTrees, preTrees) = getCustomPreTress
    val tree =
      q"""
         ..$preTrees
         ..${getAnnoClassObject[T](customTrees, format)}
         lazy val lines = $ts.map { ($innerTName: $clazzName) =>
             $csvableInstanceTermName.$innerTmpTermName = $innerTName
             $csvableInstanceTermName._toCsvString($innerTName)
         }
         $packageName.StringUtils.combineRows(lines, $format)
      """
    exprPrintTree[String](force = false, tree)
  }

  private def getAnnoClassObject[T: WeakTypeTag](customTrees: mutable.Map[String, Any], format: c.Expr[CsvFormat]): Tree = {
    val clazzName     = resolveClassTypeName[T]
    val annoClassName = TermName(csvableImplClassNamePrefix + MacroCache.getIdentityId)
    q"""
       object $annoClassName extends $packageName.Csvable[$clazzName] {
           var $innerTmpTermName: $clazzName = _
           
           lazy private val toCsv = ($funcArgsTempTermName: $clazzName) => {
                val fields = ${clazzName.toTermName}.unapply($funcArgsTempTermName).orNull
                val values = if (null == fields) List.empty else ${fieldsToString[T](funcArgsTempTermName, customTrees)}
                $packageName.StringUtils.combineColumns(values, $format)
           }
           override def _toCsvString(t: $clazzName): String = toCsv($annoClassName.$innerTmpTermName)
       }
       
       final lazy private val $csvableInstanceTermName = $annoClassName
     """
  }

  private def deriveCsvableImpl[T: WeakTypeTag](t: Expr[T], format: c.Expr[CsvFormat]): Expr[String] = {
    val clazzName               = resolveClassTypeName[T]
    val (customTrees, preTrees) = getCustomPreTress
    val annoClassName           = TermName(csvableImplClassNamePrefix + MacroCache.getIdentityId)
    val tree =
      q"""
         ..$preTrees
         object $annoClassName extends $packageName.Csvable[$clazzName] {
            final private val $innerTmpTermName = $t
           
            override def _toCsvString(t: $clazzName): String = {
                val fields = ${clazzName.toTermName}.unapply($innerTmpTermName).orNull
                val values = if (null == fields) List.empty else ${fieldsToString[T](innerTmpTermName, customTrees)}
                $packageName.StringUtils.combineColumns(values, $format)
            }
         }
         $annoClassName._toCsvString($t)
      """
    exprPrintTree[String](force = false, tree)
  }

  // scalafmt: { maxColumn = 400 }
  private def fieldsToString[T: WeakTypeTag](innerVarTermName: TermName, customTrees: mutable.Map[String, Any]): List[Tree] = {
    val clazzName                = resolveClassTypeName[T]
    val (fieldNames, indexTypes) = checkCaseClassZipParams
    val indexByName              = (i: Int) => TermName(fieldNames(i))
    indexTypes.map { idxType =>
      val customFunction = () => q"${TermName(builderFunctionPrefix + fieldNames(idxType._1))}.apply($innerVarTermName.${indexByName(idxType._1)})"
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
            // scalafmt: { maxColumn = 400 }
            q"""
              $packageName.Csvable[${genericType.typeSymbol.name.toTypeName}]._toCsvString {
                if ($innerVarTermName.${indexByName(idxType._1)}.isEmpty) "" 
                else $innerVarTermName.${indexByName(idxType._1)}.get
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
  }

}
