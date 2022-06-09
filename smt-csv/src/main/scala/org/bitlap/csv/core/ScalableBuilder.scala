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

package org.bitlap.csv.core

import org.bitlap.csv.core.macros.DeriveScalableBuilder
import java.io.InputStream

/** Builder to create a custom Csv Decoder.
 *
 *  @author
 *    梦境迷离
 *  @version 1.0,2022/4/30
 */
class ScalableBuilder[T] {

  /** Convert any Scala types to this CSV column string.
   *
   *  @param scalaField
   *    The field in scala case class.
   *  @param value
   *    This function specifies how you want to convert this CSV column to a scala type.
   *  @tparam SF
   *    The field type, generally, it is not necessary to specify, but it is safer if specify.
   *  @return
   */
  def setField[SF](scalaField: T => SF, value: String => SF): ScalableBuilder[T] =
    macro DeriveScalableBuilder.setFieldImpl[T, SF]

  /** Create a custom builder for converting this CSV line to scala values.
   *
   *  @param line
   *    One CSV line.
   *  @param format
   *    For processing CSV in the specified format.
   *  @return
   */
  def convert(line: String)(implicit format: CsvFormat): Option[T] = macro DeriveScalableBuilder.convertOneImpl[T]

  /** Convert all CSV lines to the sequence of Scala case class.
   *
   *  @param lines
   *    All CSV lines.
   *  @param format
   *    For processing CSV in the specified format.
   *  @return
   */
  def convert(lines: List[String])(implicit format: CsvFormat): List[Option[T]] =
    macro DeriveScalableBuilder.convertAllImpl[T]

  /** Read all CSV lines of the file and convert them to the sequence of Scala case class.
   *
   *  @param file
   *    InputStream of the CSV file.
   *  @param format
   *    For processing CSV in the specified format. Passing anonymous objects is not supported.
   *  @return
   */
  def convertFrom(file: InputStream)(implicit format: CsvFormat): List[Option[T]] =
    macro DeriveScalableBuilder.convertFromFileImpl[T]

}

object ScalableBuilder {

  def apply[T <: Product]: ScalableBuilder[T] = macro DeriveScalableBuilder.applyImpl[T]

}
