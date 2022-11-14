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

package org.bitlap.csv.internal

import org.bitlap.common.MacroCache
import org.bitlap.common.internal.AbstractMacroProcessor
import org.bitlap.csv.{ CsvFormat, WriterBuilder }

import java.io.File
import scala.collection.mutable
import scala.reflect.macros.whitebox

/** @author
 *    梦境迷离
 *  @version 1.0,2022/4/29
 */
class WriterBuilderMacro(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

  import c.universe._

  protected val packageName = q"_root_.org.bitlap.csv"

  private val annoBuilderPrefix = "_AnonWriterBuilder$"

  private val builderFunctionPrefix = "_WriterBuilderFunction$"

  private val innerTName                = q"_t"
  private val innerTmpTermName          = TermName("_tt")
  private val writerInstanceTermName    = TermName("_WriterInstance")
  private val writerImplClassNamePrefix = "_CSVAnno$"
  private val funcArgsTempTermName      = TermName("temp")

  // scalafmt: { maxColumn = 400 }
  def setFieldImpl[T, SF](scalaField: Expr[T => SF], value: Expr[SF => String]): Expr[WriterBuilder[T]] = {
    val Function(_, Select(_, termName)) = scalaField.tree
    val builderId                        = getBuilderId(annoBuilderPrefix)
    MacroCache.builderFunctionTrees.getOrElseUpdate(builderId, mutable.Map.empty).update(termName.toString, value)
    val tree = q"new ${c.prefix.actualType}"
    c.Expr[WriterBuilder[T]](tree)
  }

  def applyImpl[T: WeakTypeTag]: Expr[WriterBuilder[T]] =
    deriveBuilderApplyImpl[T]

  def convertOneImpl[T: WeakTypeTag](t: Expr[T])(format: c.Expr[CsvFormat]): Expr[String] =
    deriveWriterImpl[T](t, format)

  def convertAllImpl[T: WeakTypeTag](ts: Expr[List[T]])(format: c.Expr[CsvFormat]): Expr[String] =
    deriveFullWriterImpl[T](ts, format)

  def convertToFileImpl[T: WeakTypeTag](ts: Expr[List[T]], file: Expr[File])(format: c.Expr[CsvFormat]): Expr[Boolean] =
    deriveFullIntoFileWriterImpl[T](ts, file, format)

  private def deriveBuilderApplyImpl[T: WeakTypeTag]: Expr[WriterBuilder[T]] = {
    val className     = TypeName(annoBuilderPrefix + MacroCache.getBuilderId)
    val caseClazzName = TypeName(weakTypeOf[T].typeSymbol.name.decodedName.toString)
    val tree =
      q"""
        class $className extends $packageName.WriterBuilder[$caseClazzName]
        new $className
      """
    c.Expr[WriterBuilder[T]](tree)
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
  private def deriveFullIntoFileWriterImpl[T: WeakTypeTag](ts: Expr[List[T]], file: Expr[File], format: c.Expr[CsvFormat]): Expr[Boolean] = {
    val clazzName               = classTypeName[T]
    val (customTrees, preTrees) = getCustomPreTress
    val tree =
      q"""
         ..$preTrees
         ..${getAnnoClassObject[T](customTrees, format)}
         $packageName.FileUtils.writer($file, $ts.map { ($innerTName: $clazzName) =>
               $writerInstanceTermName.$innerTmpTermName = $innerTName
               $writerInstanceTermName.transform($innerTName)
           }, $format
         )
      """
    c.Expr[Boolean](tree)
  }

  // scalafmt: { maxColumn = 400 }
  private def deriveFullWriterImpl[T: WeakTypeTag](ts: Expr[List[T]], format: c.Expr[CsvFormat]): Expr[String] = {
    val clazzName               = classTypeName[T]
    val (customTrees, preTrees) = getCustomPreTress
    val tree =
      q"""
         ..$preTrees
         ..${getAnnoClassObject[T](customTrees, format)}
         lazy val lines = $ts.map { ($innerTName: $clazzName) =>
             $writerInstanceTermName.$innerTmpTermName = $innerTName
             $writerInstanceTermName.transform($innerTName)
         }
         $packageName.StringUtils.combineRows(lines, $format)
      """
    c.Expr[String](tree)
  }

  private def getAnnoClassObject[T: WeakTypeTag](customTrees: mutable.Map[String, Any], format: c.Expr[CsvFormat]): Tree = {
    val clazzName     = classTypeName[T]
    val annoClassName = TermName(writerImplClassNamePrefix + MacroCache.getIdentityId)
    q"""
       object $annoClassName extends $packageName.Writer[$clazzName] {
           var $innerTmpTermName: $clazzName = _
           
           lazy private val _toCsv = ($funcArgsTempTermName: $clazzName) => {
                val fields = ${clazzName.toTermName}.unapply($funcArgsTempTermName).orNull
                val values = if (null == fields) _root_.scala.List.empty else ${fieldsToString[T](funcArgsTempTermName, customTrees)}
                $packageName.StringUtils.combineColumns(values, $format)
           }
           override def transform(t: $clazzName): String = _toCsv($annoClassName.$innerTmpTermName)
       }
       
       final lazy private val $writerInstanceTermName = $annoClassName
     """
  }

  private def deriveWriterImpl[T: WeakTypeTag](t: Expr[T], format: c.Expr[CsvFormat]): Expr[String] = {
    val clazzName               = classTypeName[T]
    val (customTrees, preTrees) = getCustomPreTress
    val annoClassName           = TermName(writerImplClassNamePrefix + MacroCache.getIdentityId)
    val tree =
      q"""
         ..$preTrees
         object $annoClassName extends $packageName.Writer[$clazzName] {
            final private val $innerTmpTermName = $t
           
            override def transform(t: $clazzName): String = {
                val fields = ${clazzName.toTermName}.unapply($innerTmpTermName).orNull
                val values = if (null == fields) _root_.scala.List.empty else ${fieldsToString[T](innerTmpTermName, customTrees)}
                $packageName.StringUtils.combineColumns(values, $format)
            }
         }
         $annoClassName.transform($t)
      """
    c.Expr[String](tree)
  }

  // scalafmt: { maxColumn = 400 }
  private def fieldsToString[T: WeakTypeTag](innerVarTermName: TermName, customTrees: mutable.Map[String, Any]): List[Tree] = {
    val clazzName               = classTypeName[T]
    val fieldZipInformationList = fieldZipInformation[T]
    val fieldNames              = fieldZipInformationList.fieldNames
    val indexTypes              = fieldZipInformationList.fieldIndexTypeMapping
    val indexByName             = (i: Int) => TermName(fieldNames(i))
    indexTypes.map { indexType =>
      val customFunction = () => q"${TermName(builderFunctionPrefix + fieldNames(indexType._1))}.apply($innerVarTermName.${indexByName(indexType._1)})"
      indexType._2 match {
        case t if t <:< typeOf[List[_]] && customTrees.contains(fieldNames(indexType._1)) =>
          q"${customFunction()}"
        case t if t <:< typeOf[List[_]] && !customTrees.contains(fieldNames(indexType._1)) =>
          c.abort(
            c.enclosingPosition,
            s"Missing usage `setField` for converting `$clazzName.${fieldNames(indexType._1)}` as a `String` , you have to define a custom way by using `setField` method!"
          )
        case t if t <:< typeOf[Seq[_]] && customTrees.contains(fieldNames(indexType._1)) =>
          q"${customFunction()}"
        case t if t <:< typeOf[Seq[_]] && !customTrees.contains(fieldNames(indexType._1)) =>
          c.abort(
            c.enclosingPosition,
            s"Missing usage `setField` for converting `$clazzName.${fieldNames(indexType._1)}` as a `String` , you have to define a custom way by using `setField` method!"
          )
        case t if t <:< typeOf[Set[_]] && customTrees.contains(fieldNames(indexType._1)) =>
          q"${customFunction()}"
        case t if t <:< typeOf[Set[_]] && !customTrees.contains(fieldNames(indexType._1)) =>
          c.abort(
            c.enclosingPosition,
            s"Missing usage `setField` for converting `$clazzName.${fieldNames(indexType._1)}` as a `String` , you have to define a custom way by using `setField` method!"
          )
        case t if t <:< typeOf[Vector[_]] && customTrees.contains(fieldNames(indexType._1)) =>
          q"${customFunction()}"
        case t if t <:< typeOf[Vector[_]] && !customTrees.contains(fieldNames(indexType._1)) =>
          c.abort(
            c.enclosingPosition,
            s"Missing usage `setField` for converting `$clazzName.${fieldNames(indexType._1)}` as a `String` , you have to define a custom way by using `setField` method!"
          )
        case t if t <:< typeOf[Option[_]] && customTrees.contains(fieldNames(indexType._1)) =>
          customFunction()
        case t if t <:< typeOf[Option[_]] && !customTrees.contains(fieldNames(indexType._1)) =>
          val genericType = c.typecheck(q"${indexType._2}", c.TYPEmode).tpe.dealias.typeArgs.head
          q"""
              $packageName.Writer[$genericType].transform {
                if ($innerVarTermName.${indexByName(indexType._1)}.isEmpty) "" 
                else $innerVarTermName.${indexByName(indexType._1)}.get
              }
            """
        case _ if customTrees.contains(fieldNames(indexType._1)) =>
          customFunction()
        case _ =>
          q"$packageName.Writer[${indexType._2}].transform($innerVarTermName.${indexByName(indexType._1)})"
      }
    }
  }

}
