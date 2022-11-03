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

package org.bitlap.csv

import org.bitlap.csv.internal.WriterBuilderMacro
import java.io.File

/** Builder to create a custom Csv Encoder.
 *
 *  @author
 *    梦境迷离
 *  @version 1.0,2022/4/30
 */
class WriterBuilder[T] {

  /** Convert this CSV column string to any Scala types.
   *
   *  @param scalaField
   *    The field in scala case class.
   *  @param value
   *    This function specifies how you want to convert this field to a CSV string.
   *  @tparam SF
   *    The field type, generally, it is not necessary to specify, but it is safer if specify.
   *  @return
   */
  def setField[SF](scalaField: T => SF, value: SF => String): WriterBuilder[T] =
    macro WriterBuilderMacro.setFieldImpl[T, SF]

  /** Create a custom builder for converting this scala value to CSV line string.
   *
   *  @param t
   *    The value of Scala case class.
   *  @param format
   *    For processing CSV in the specified format.
   *  @return
   *    The string of one CSV line.
   */
  def convert(t: T)(implicit format: CsvFormat): String = macro WriterBuilderMacro.convertOneImpl[T]

  /** Convert the sequence of Scala case class to CSV string.
   *
   *  @param ts
   *    The sequence of Scala case class.
   *  @param format
   *    For processing CSV in the specified format.
   *  @return
   *    The string of all CSV lines.
   */
  def convert(ts: List[T])(implicit format: CsvFormat): String = macro WriterBuilderMacro.convertAllImpl[T]

  /** Convert the sequence of Scala case class to CSV string and write to file.
   *
   *  @param ts
   *    The sequence of Scala case class.
   *  @param file
   *    File to save CSV string.
   *  @param format
   *    For processing CSV in the specified format. Passing anonymous objects is not supported.
   *  @return
   *    The string of all CSV lines.
   */
  def convertTo(ts: List[T], file: File)(implicit format: CsvFormat): Boolean =
    macro WriterBuilderMacro.convertToFileImpl[T]

}

object WriterBuilder {

  def apply[T <: Product]: WriterBuilder[T] = macro WriterBuilderMacro.applyImpl[T]

}
