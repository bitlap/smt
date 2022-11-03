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
trait WriterImplicits {

  implicit final val stringCsvable: Writer[String] = new Writer[String] {
    override def transform(s: String): String = s
  }

  implicit final val intCsvable: Writer[Int] = new Writer[Int] {
    override def transform(column: Int): String = column.toString
  }

  implicit final val charCsvable: Writer[Char] = new Writer[Char] {
    override def transform(t: Char): String = t.toString
  }

  implicit final val longCsvable: Writer[Long] = new Writer[Long] {
    override def transform(column: Long): String = column.toString
  }

  implicit final val shortCsvable: Writer[Short] = new Writer[Short] {
    override def transform(column: Short): String = column.toString
  }

  implicit final val doubleCsvable: Writer[Double] = new Writer[Double] {
    override def transform(column: Double): String = column.toString
  }

  implicit final val floatCsvable: Writer[Float] = new Writer[Float] {
    override def transform(column: Float): String = column.toString
  }

  implicit final val booleanCsvable: Writer[Boolean] = new Writer[Boolean] {
    override def transform(column: Boolean): String = column.toString
  }
}
