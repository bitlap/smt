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
import org.bitlap.csv.{ CsvFormat, ReaderBuilder }

import java.io.InputStream
import scala.collection.mutable
import scala.reflect.macros.whitebox

/** @author
 *    梦境迷离
 *  @version 1.0,2022/4/29
 */
class ReaderBuilderMacro(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

  import c.universe._

  protected val packageName = q"_root_.org.bitlap.csv"

  private val annoBuilderPrefix = "_AnonReaderBuilder$"

  private val builderFunctionPrefix = "_ReaderBuilderFunction$"

  private val innerColumnFuncTermName   = TermName("_columns")
  private val innerLName                = q"_l"
  private val innerTempTermName         = TermName("_line")
  private val readerInstanceTermName    = TermName("_ReaderInstance")
  private val readerImplClassNamePrefix = "_ScalaAnno$"

  // scalafmt: { maxColumn = 400 }
  @unchecked
  def setFieldImpl[T, SF](scalaField: Expr[T => SF], value: Expr[String => SF]): Expr[ReaderBuilder[T]] = {
    val Function(_, Select(_, termName)) = scalaField.tree
    val builderId                        = getBuilderId(annoBuilderPrefix)
    MacroCache.builderFunctionTrees.getOrElseUpdate(builderId, mutable.Map.empty).update(termName.toString, value)
    val tree = q"new ${c.prefix.actualType}"
    c.Expr[ReaderBuilder[T]](tree)
  }

  def applyImpl[T: WeakTypeTag]: Expr[ReaderBuilder[T]] =
    deriveBuilderApplyImpl[T]

  def convertOneImpl[T: WeakTypeTag](line: Expr[String])(format: c.Expr[CsvFormat]): Expr[Option[T]] = {
    val clazzName = classTypeName[T]
    deriveReaderImpl[T](clazzName, line, format)
  }

  def convertAllImpl[T: WeakTypeTag](lines: Expr[List[String]])(format: c.Expr[CsvFormat]): Expr[List[Option[T]]] = {
    val clazzName = classTypeName[T]
    deriveFullReaderImpl[T](clazzName, lines, format)
  }

  def convertFromFileImpl[T: WeakTypeTag](file: Expr[InputStream])(format: c.Expr[CsvFormat]): Expr[List[Option[T]]] = {
    val clazzName = classTypeName[T]
    deriveFullFromFileReaderImpl[T](clazzName, file, format)
  }

  private def deriveBuilderApplyImpl[T: WeakTypeTag]: Expr[ReaderBuilder[T]] = {
    val className     = TypeName(annoBuilderPrefix + MacroCache.getBuilderId)
    val caseClazzName = weakTypeOf[T].typeSymbol.name.toTypeName
    val tree =
      q"""
        class $className extends $packageName.ReaderBuilder[$caseClazzName]
        new $className
       """
    c.Expr[ReaderBuilder[T]](tree)
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
  private def deriveFullFromFileReaderImpl[T: WeakTypeTag](clazzName: TypeName, file: Expr[InputStream], format: c.Expr[CsvFormat]): Expr[List[Option[T]]] = {
    // NOTE: preTrees must be at the same level as Reader
    val tree =
      q"""
         ..$getPreTree
         ..${getAnnoClassObject[T](clazzName, format)}
         $packageName.FileUtils.read($file, $format).map { ($innerLName: String) =>
             $readerInstanceTermName.$innerTempTermName = ${TermName(innerLName.toString())}
             $readerInstanceTermName.transform($innerLName) 
         }
      """
    c.Expr[List[Option[T]]](tree)
  }

  // scalafmt: { maxColumn = 400 }
  private def deriveFullReaderImpl[T: WeakTypeTag](clazzName: TypeName, lines: Expr[List[String]], format: c.Expr[CsvFormat]): Expr[List[Option[T]]] = {
    // NOTE: preTrees must be at the same level as Reader
    val tree =
      q"""
         ..$getPreTree
         ..${getAnnoClassObject[T](clazzName, format)}
         $lines.map { ($innerLName: String) =>
             $readerInstanceTermName.$innerTempTermName = ${TermName(innerLName.toString())}
             $readerInstanceTermName.transform($innerLName) 
         }
      """
    c.Expr[List[Option[T]]](tree)
  }

  private def getAnnoClassObject[T: WeakTypeTag](clazzName: TypeName, format: c.Expr[CsvFormat]): Tree = {
    val annoClassName = TermName(readerImplClassNamePrefix + MacroCache.getIdentityId)
    q"""
       object $annoClassName extends $packageName.Reader[$clazzName] {
           var $innerTempTermName: String = _
           private val $innerColumnFuncTermName = () => $packageName.StringUtils.splitColumns(${annoClassName.toTermName}.$innerTempTermName, $format)
            ..${readerBody[T](clazzName, innerColumnFuncTermName)}
       }
       private final lazy val $readerInstanceTermName = $annoClassName
     """
  }

  // scalafmt: { maxColumn = 400 }
  private def deriveReaderImpl[T: WeakTypeTag](clazzName: TypeName, line: Expr[String], format: c.Expr[CsvFormat]): Expr[Option[T]] = {
    val annoClassName = TermName(readerImplClassNamePrefix + MacroCache.getIdentityId)
    // NOTE: preTrees must be at the same level as Reader
    val tree =
      q"""
         ..$getPreTree
         object $annoClassName extends $packageName.Reader[$clazzName] {
            final lazy private val $innerColumnFuncTermName = () => $packageName.StringUtils.splitColumns($line, $format)
            ..${readerBody[T](clazzName, innerColumnFuncTermName)}
         }
         $annoClassName.transform($line)
      """
    c.Expr[Option[T]](tree)
  }

  // scalafmt: { maxColumn = 400 }
  private def readerBody[T: WeakTypeTag](clazzName: TypeName, innerFuncTermName: TermName): Tree = {
    val customTrees = MacroCache.builderFunctionTrees.getOrElse(getBuilderId(annoBuilderPrefix), mutable.Map.empty)
    val params      = caseClassFieldInfos[T]()
    val fieldNames  = params.map(_.fieldName)
    val fields = fieldTreeInformation[T](innerFuncTermName).map { fieldTreeInformation =>
      val idx            = fieldTreeInformation.index
      val columnValues   = fieldTreeInformation.fieldTerm
      val fieldType      = fieldTreeInformation.fieldType
      val fieldTypeName  = fieldType.typeSymbol.name.toTypeName
      val customFunction = () => q"${TermName(builderFunctionPrefix + fieldNames(idx))}.apply($columnValues)"
      fieldTreeInformation.genericType match {
        case Nil if customTrees.contains(fieldNames(idx)) =>
          tryGetOrElse(q"${customFunction()}.asInstanceOf[$fieldTypeName]", fieldTreeInformation.zeroValue)
        case Nil if !customTrees.contains(fieldNames(idx)) =>
          fieldType match {
            case t if t =:= typeOf[Int] =>
              q"$packageName.Reader[$fieldTypeName].transform($columnValues).getOrElse(${fieldTreeInformation.zeroValue})"
            case t if t =:= typeOf[String] =>
              q"""$packageName.Reader[$fieldTypeName].transform($columnValues).getOrElse(${fieldTreeInformation.zeroValue})"""
            case t if t =:= typeOf[Float] =>
              q"$packageName.Reader[$fieldTypeName].transform($columnValues).getOrElse[Float](${fieldTreeInformation.zeroValue})"
            case t if t =:= typeOf[Double] =>
              q"$packageName.Reader[$fieldTypeName].transform($columnValues).getOrElse[Double](${fieldTreeInformation.zeroValue})"
            case t if t =:= typeOf[Char] =>
              q"$packageName.Reader[$fieldTypeName].transform($columnValues).getOrElse(${fieldTreeInformation.zeroValue})"
            case t if t =:= typeOf[Byte] =>
              q"$packageName.Reader[$fieldTypeName].transform($columnValues).getOrElse(${fieldTreeInformation.zeroValue})"
            case t if t =:= typeOf[Short] =>
              q"$packageName.Reader[$fieldTypeName].transform($columnValues).getOrElse(${fieldTreeInformation.zeroValue})"
            case t if t =:= typeOf[Boolean] =>
              q"$packageName.Reader[$fieldTypeName].transform($columnValues).getOrElse(${fieldTreeInformation.zeroValue})"
            case t if t =:= typeOf[Long] =>
              q"$packageName.Reader[$fieldTypeName].transform($columnValues).getOrElse(${fieldTreeInformation.zeroValue})"
            case _ =>
              tryOptionGetOrElse(q"$packageName.Reader[$fieldTypeName].transform($columnValues)", fieldTreeInformation.zeroValue)
          }
        case generic :: Nil if customTrees.contains(fieldNames(idx)) && fieldTreeInformation.collectionsFlags.isList =>
          tryGetOrElse(q"${customFunction()}.asInstanceOf[_root_.scala.List[$generic]]", fieldTreeInformation.zeroValue)
        case generic :: Nil if customTrees.contains(fieldNames(idx)) && fieldTreeInformation.collectionsFlags.isSet =>
          tryGetOrElse(q"${customFunction()}.asInstanceOf[_root_.scala.Predef.Set[$generic]]", fieldTreeInformation.zeroValue)
        case generic :: Nil if customTrees.contains(fieldNames(idx)) && fieldTreeInformation.collectionsFlags.isVector =>
          tryGetOrElse(q"${customFunction()}.asInstanceOf[_root_.scala.Vector[$generic]]", fieldTreeInformation.zeroValue)
        case generic :: Nil if customTrees.contains(fieldNames(idx)) && fieldTreeInformation.collectionsFlags.isOption =>
          tryGetOrElse(q"${customFunction()}.asInstanceOf[_root_.scala.Option[$generic]]", fieldTreeInformation.zeroValue)
        case generic :: Nil if customTrees.contains(fieldNames(idx)) && fieldTreeInformation.collectionsFlags.isSeq =>
          tryGetOrElse(q"${customFunction()}.asInstanceOf[_root_.scala.Seq[$generic]]", fieldTreeInformation.zeroValue)
        case generic :: Nil if fieldTreeInformation.collectionsFlags.isOption =>
          tryOption(q"$packageName.Reader[$generic].transform($columnValues)")
        case generic =>
          c.abort(
            c.enclosingPosition,
            s"Missing usage `setField` for parsing `$clazzName.${fieldNames(idx)}` as a `$fieldType` with genericType: `$generic`, you have to define a custom way by using `setField` method!"
          )
      }
    }

    // input args not need used
    q"""override def transform(column: String): _root_.scala.Option[$clazzName] = 
       ${tryOption(q"_root_.scala.Option(${clazzName.toTermName}(..$fields))")}"""
  }
}
