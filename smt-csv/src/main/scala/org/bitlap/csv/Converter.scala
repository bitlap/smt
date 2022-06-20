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

import scala.collection.immutable.{ :: => Cons }
import scala.util.Try

/** Csv encoder and decoder.
 *
 *  @author
 *    梦境迷离
 *  @since 2022/04/27
 *  @version 1.0
 */
trait Converter[T] {

  def toScala(line: String): Option[T]

  def toCsvString(t: T): String
}

object Converter {

  def apply[T](implicit st: Converter[T], format: CsvFormat): Converter[T] = st

  // Primitives
  implicit final val stringCSVConverter: Converter[String] = new Converter[String] {
    def toScala(line: String): Option[String] = if (line.isEmpty) None else Some(line)

    def toCsvString(s: String): String = s
  }

  implicit final val intCsvConverter: Converter[Int] = new Converter[Int] {
    def toScala(line: String): Option[Int] = Try(line.toInt).toOption

    def toCsvString(i: Int): String = i.toString
  }

  implicit final val charCsvConverter: Converter[Char] = new Converter[Char] {
    def toScala(line: String): Option[Char] = if (line.isEmpty) None else Try(line.charAt(0)).toOption

    override def toCsvString(t: Char): String = t.toString
  }

  implicit final val longCsvConverter: Converter[Long] = new Converter[Long] {
    def toScala(line: String): Option[Long] = Try(line.toLong).toOption

    def toCsvString(i: Long): String = i.toString
  }

  implicit final val shortCsvConverter: Converter[Short] = new Converter[Short] {
    def toScala(line: String): Option[Short] = Try(line.toShort).toOption

    def toCsvString(i: Short): String = i.toString
  }

  implicit final val doubleCsvConverter: Converter[Double] = new Converter[Double] {
    def toScala(line: String): Option[Double] = Try(line.toDouble).toOption

    def toCsvString(i: Double): String = i.toString
  }

  implicit final val floatCsvConverter: Converter[Float] = new Converter[Float] {
    def toScala(line: String): Option[Float] = Try(line.toFloat).toOption

    def toCsvString(i: Float): String = i.toString
  }

  implicit final val booleanCsvConverter: Converter[Boolean] = new Converter[Boolean] {
    def toScala(line: String): Option[Boolean] = Try(line.toBoolean).toOption

    def toCsvString(i: Boolean): String = i.toString
  }

  @inline private[this] final def listCsvLinesConverter[A](
    l: List[String]
  )(implicit ec: Converter[A]): Option[List[A]] =
    l match {
      case Nil => Some(Nil)
      case Cons(s, ss) =>
        for {
          x  <- Try(ec.toScala(s)).getOrElse(None)
          xs <- listCsvLinesConverter(ss)(ec)
        } yield Cons(x, xs)
    }

  implicit final def listCsvConverter[A <: Product](implicit ec: Converter[A], format: CsvFormat): Converter[List[A]] =
    new Converter[List[A]] {
      val lineSeparator = format.lineTerminator

      def toScala(line: String): Option[List[A]] = listCsvLinesConverter[A](line.split(lineSeparator).toList)(ec)

      def toCsvString(l: List[A]): String =
        if (l == null) "" else l.map(ec.toCsvString).mkString(lineSeparator)
    }
}
