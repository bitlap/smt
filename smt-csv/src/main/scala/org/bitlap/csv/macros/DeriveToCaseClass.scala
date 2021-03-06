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

import org.bitlap.common.internal.AbstractMacroProcessor
import org.bitlap.csv.CsvFormat

import scala.reflect.macros.blackbox

/** @author
 *    梦境迷离
 *  @version 1.0,2022/4/29
 */
object DeriveToCaseClass {

  def apply[T <: Product](line: String)(implicit format: CsvFormat): Option[T] = macro Macro.macroImpl[T]

  class Macro(override val c: blackbox.Context) extends AbstractMacroProcessor(c) {

    import c.universe._

    protected val packageName = q"_root_.org.bitlap.csv"

    // scalafmt: { maxColumn = 400 }
    def macroImpl[T <: Product: c.WeakTypeTag](line: c.Expr[String])(format: c.Expr[CsvFormat]): c.Expr[Option[T]] = {
      val clazzName         = c.weakTypeOf[T].typeSymbol.name
      val innerFuncTermName = TermName("_columns")
      val fields = (columnsFunc: TermName) =>
        checkGetFieldTreeInformationList[T](columnsFunc).map { fieldTreeInformation =>
          val columnValues = fieldTreeInformation.fieldTerm
          val fieldType    = fieldTreeInformation.fieldType
          fieldTreeInformation.genericType match {
            case generic :: Nil if fieldTreeInformation.collectionsFlags.isList =>
              tryOptionGetOrElse(q"$packageName.Converter[_root_.scala.List[$generic]].toScala($columnValues)", fieldTreeInformation.zeroValue)
            case generic :: Nil if fieldTreeInformation.collectionsFlags.isSet =>
              tryOptionGetOrElse(q"$packageName.Converter[_root_.scala.Predef.Set[$generic]].toScala($columnValues)", fieldTreeInformation.zeroValue)
            case generic :: Nil if fieldTreeInformation.collectionsFlags.isVector =>
              tryOptionGetOrElse(q"$packageName.Converter[_root_.scala.Vector[$generic]].toScala($columnValues)", fieldTreeInformation.zeroValue)
            case generic :: Nil if fieldTreeInformation.collectionsFlags.isSeq =>
              tryOptionGetOrElse(q"$packageName.Converter[_root_.scala.Seq[$generic]].toScala($columnValues)", fieldTreeInformation.zeroValue)
            case generic :: Nil if fieldTreeInformation.collectionsFlags.isOption =>
              tryOption(q"$packageName.Converter[$generic].toScala($columnValues)")
            case generic :: Nil =>
              c.abort(
                c.enclosingPosition,
                s"Not support `$fieldType` with genericType: `$generic`!!!"
              )
            case _ =>
              val caseClassFieldTypeName = fieldType.typeSymbol.name.toTypeName
              fieldType match {
                case tt if tt =:= typeOf[Int] =>
                  q"$packageName.Converter[$caseClassFieldTypeName].toScala($columnValues).getOrElse(${fieldTreeInformation.zeroValue})"
                case tt if tt =:= typeOf[String] =>
                  q"""$packageName.Converter[$caseClassFieldTypeName].toScala($columnValues).getOrElse(${fieldTreeInformation.zeroValue})"""
                case tt if tt =:= typeOf[Float] =>
                  q"$packageName.Converter[$caseClassFieldTypeName].toScala($columnValues).getOrElse(${fieldTreeInformation.zeroValue})"
                case tt if tt =:= typeOf[Double] =>
                  q"$packageName.Converter[$caseClassFieldTypeName].toScala($columnValues).getOrElse(${fieldTreeInformation.zeroValue})"
                case tt if tt =:= typeOf[Char] =>
                  q"$packageName.Converter[$caseClassFieldTypeName].toScala($columnValues).getOrElse(${fieldTreeInformation.zeroValue})"
                case tt if tt =:= typeOf[Byte] =>
                  q"$packageName.Converter[$caseClassFieldTypeName].toScala($columnValues).getOrElse(${fieldTreeInformation.zeroValue})"
                case tt if tt =:= typeOf[Short] =>
                  q"$packageName.Converter[$caseClassFieldTypeName].toScala($columnValues).getOrElse(${fieldTreeInformation.zeroValue})"
                case tt if tt =:= typeOf[Boolean] =>
                  q"$packageName.Converter[$caseClassFieldTypeName].toScala($columnValues).getOrElse(${fieldTreeInformation.zeroValue})"
                case tt if tt =:= typeOf[Long] =>
                  q"$packageName.Converter[$caseClassFieldTypeName].toScala($columnValues).getOrElse(${fieldTreeInformation.zeroValue})"
                case _ =>
                  tryOptionGetOrElse(q"$packageName.Converter[$caseClassFieldTypeName].toScala($columnValues)", fieldTreeInformation.zeroValue)
              }
          }
        }
      val tree =
        q"""
           lazy val $innerFuncTermName = () => $packageName.StringUtils.splitColumns($line, $format)
           _root_.scala.Option(${clazzName.toTermName}(..${fields(innerFuncTermName)}))
           """
      exprPrintTree[T](force = false, tree)

    }.asInstanceOf[c.Expr[Option[T]]]

  }
}
