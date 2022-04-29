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

import scala.collection.immutable.{ :: => Cons }

/**
 * Csv encoder and decoder api.
 *
 * @author 梦境迷离
 * @since 2022/04/27
 * @version 1.0
 */
trait CsvConverter[T] {

  def from(s: String): Option[T]

  def to(t: T): String
}

object CsvConverter {

  lazy val LINE_SEPARATOR: String = "\n"

  def apply[T](implicit st: => CsvConverter[T]): CsvConverter[T] = st

  // Primitives
  implicit val stringCSVConverter: CsvConverter[String] = new CsvConverter[String] {
    def from(s: String): Option[String] = if (s.isEmpty) None else Some(s)

    def to(s: String): String = s
  }

  implicit val intCsvConverter: CsvConverter[Int] = new CsvConverter[Int] {
    def from(s: String): Option[Int] = Option(s.toInt)

    def to(i: Int): String = i.toString
  }

  implicit val charCsvConverter: CsvConverter[Char] = new CsvConverter[Char] {
    def from(s: String): Option[Char] = if (s.isEmpty) None else Some(s.charAt(0))

    override def to(t: Char): String = t.toString
  }

  implicit val longCsvConverter: CsvConverter[Long] = new CsvConverter[Long] {
    def from(s: String): Option[Long] = Option(s.toLong)

    def to(i: Long): String = i.toString
  }

  implicit val shortCsvConverter: CsvConverter[Short] = new CsvConverter[Short] {
    def from(s: String): Option[Short] = Option(s.toShort)

    def to(i: Short): String = i.toString
  }

  implicit val doubleCsvConverter: CsvConverter[Double] = new CsvConverter[Double] {
    def from(s: String): Option[Double] = Option(s.toDouble)

    def to(i: Double): String = i.toString
  }

  implicit val floatCsvConverter: CsvConverter[Float] = new CsvConverter[Float] {
    def from(s: String): Option[Float] = Option(s.toFloat)

    def to(i: Float): String = i.toString
  }

  implicit val booleanCsvConverter: CsvConverter[Boolean] = new CsvConverter[Boolean] {
    def from(s: String): Option[Boolean] = Option(s.toBoolean)

    def to(i: Boolean): String = i.toString
  }

  @inline private[this] def listCsvLinesConverter[A](l: List[String])(implicit ec: CsvConverter[A]): Option[List[A]] = l match {
    case Nil => Some(Nil)
    case Cons(s, ss) =>
      for {
        x <- ec.from(s)
        xs <- listCsvLinesConverter(ss)(ec)
      } yield Cons(x, xs)
  }

  implicit def listCsvConverter[A <: Product](implicit ec: CsvConverter[A]): CsvConverter[List[A]] = new CsvConverter[List[A]] {
    def from(s: String): Option[List[A]] = listCsvLinesConverter[A](s.split(LINE_SEPARATOR).toList)(ec)

    def to(l: List[A]): String = l.map(ec.to).mkString(LINE_SEPARATOR)
  }
}
