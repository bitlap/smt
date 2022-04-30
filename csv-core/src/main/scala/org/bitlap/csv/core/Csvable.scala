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

/**
 * Csv encoder.
 *
 * @author 梦境迷离
 * @since 2022/04/27
 * @version 1.0
 */
trait Csvable[T] {

  def toCsvString(t: T): String

}

object Csvable {

  lazy val LINE_SEPARATOR: String = "\n"

  def apply[T](implicit st: Csvable[T]): Csvable[T] = st

  // Primitives
  implicit val stringCSVConverter: Csvable[String] = (s: String) => s

  implicit val intCsvConverter: Csvable[Int] = (i: Int) => i.toString

  implicit val charCsvConverter: Csvable[Char] = (t: Char) => t.toString

  implicit val longCsvConverter: Csvable[Long] = (i: Long) => i.toString

  implicit val shortCsvConverter: Csvable[Short] = (i: Short) => i.toString

  implicit val doubleCsvConverter: Csvable[Double] = (i: Double) => i.toString

  implicit val floatCsvConverter: Csvable[Float] = (i: Float) => i.toString

  implicit val booleanCsvConverter: Csvable[Boolean] = (i: Boolean) => i.toString

  implicit def listCsvConverter[A <: Product](implicit ec: Csvable[A]): Csvable[List[A]] = (l: List[A]) => {
    if (l == null) "" else l.map(ec.toCsvString).mkString(LINE_SEPARATOR)
  }
}

