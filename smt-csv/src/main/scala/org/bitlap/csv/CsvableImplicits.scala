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

/** @author
 *    梦境迷离
 *  @version 1.0,2022/5/1
 */
trait CsvableImplicits {

  implicit final val stringCsvable: Csvable[String] = new Csvable[String] {
    override def _toCsvString(s: String): String = s
  }

  implicit final val intCsvable: Csvable[Int] = new Csvable[Int] {
    override def _toCsvString(column: Int): String = column.toString
  }

  implicit final val charCsvable: Csvable[Char] = new Csvable[Char] {
    override def _toCsvString(t: Char): String = t.toString
  }

  implicit final val longCsvable: Csvable[Long] = new Csvable[Long] {
    override def _toCsvString(column: Long): String = column.toString
  }

  implicit final val shortCsvable: Csvable[Short] = new Csvable[Short] {
    override def _toCsvString(column: Short): String = column.toString
  }

  implicit final val doubleCsvable: Csvable[Double] = new Csvable[Double] {
    override def _toCsvString(column: Double): String = column.toString
  }

  implicit final val floatCsvable: Csvable[Float] = new Csvable[Float] {
    override def _toCsvString(column: Float): String = column.toString
  }

  implicit final val booleanCsvable: Csvable[Boolean] = new Csvable[Boolean] {
    override def _toCsvString(column: Boolean): String = column.toString
  }
}
