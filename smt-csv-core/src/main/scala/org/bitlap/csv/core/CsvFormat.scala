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

trait CsvFormat extends Serializable {
  val delimiter: Char
  val escapeChar: Char
  val lineTerminator: String

  /** Mode for writing string into files.
   */
  val append: Boolean = false

  /** Character encoding of the file.
   */
  val encoding: String = "utf-8"

  /** Write the column name in the first row.
   */
  val headerRow: List[String] = Nil

  /** Ignore the first row when reading from file.
   */
  val ignoreHeader: Boolean = false

  /** Ignore empty lines when reading, or ignore empty strings when writing.
   */
  val ignoreEmptyLines: Boolean = false
}

trait DefaultCsvFormat extends CsvFormat {
  val delimiter: Char        = ','
  val escapeChar: Char       = '"'
  val lineTerminator: String = "\n"
}

trait TsvFormat extends CsvFormat {
  val delimiter: Char        = '\t'
  val escapeChar: Char       = '\\'
  val lineTerminator: String = "\n"
}
