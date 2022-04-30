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
 * Csv decoder.
 *
 * @author 梦境迷离
 * @since 2022/04/30
 * @version 1.0
 */
trait Scalable[T] {

  private[core] def _toScala(line: String): Option[T] = None

  def toScala: Option[T]
}

object Scalable {

  lazy val LINE_SEPARATOR: String = "\n"

  def apply[T](implicit st: Scalable[T]): Scalable[T] = st

  // Primitives
  implicit val stringCSVConverter: Scalable[String] = new Scalable[String] {
    override def _toScala(line: String): Option[String] = if (line.isEmpty) None else Some(line)

    override def toScala: Option[String] = None
  }

  implicit val intCsvConverter: Scalable[Int] = new Scalable[Int] {
    override def _toScala(line: String): Option[Int] = Option(line.toInt)

    override def toScala: Option[Int] = None

  }

  implicit val charCsvConverter: Scalable[Char] = new Scalable[Char] {
    override def _toScala(line: String): Option[Char] = if (line.isEmpty) None else Some(line.charAt(0))

    override def toScala: Option[Char] = None
  }

  implicit val longCsvConverter: Scalable[Long] = new Scalable[Long] {
    override def _toScala(line: String): Option[Long] = Option(line.toLong)

    override def toScala: Option[Long] = None
  }

  implicit val shortCsvConverter: Scalable[Short] = new Scalable[Short] {
    override def _toScala(line: String): Option[Short] = Option(line.toShort)

    override def toScala: Option[Short] = None
  }

  implicit val doubleCsvConverter: Scalable[Double] = new Scalable[Double] {
    override def _toScala(line: String): Option[Double] = Option(line.toDouble)

    override def toScala: Option[Double] = None
  }

  implicit val floatCsvConverter: Scalable[Float] = new Scalable[Float] {
    override def _toScala(line: String): Option[Float] = Option(line.toFloat)

    override def toScala: Option[Float] = None
  }

  implicit val booleanCsvConverter: Scalable[Boolean] = new Scalable[Boolean] {
    override def _toScala(line: String): Option[Boolean] = Option(line.toBoolean)

    override def toScala: Option[Boolean] = None
  }

  @inline private[this] def listCsvLinesConverter[A](l: List[String])(implicit ec: Scalable[A]): Option[List[A]] = l match {
    case Nil => Some(Nil)
    case Cons(s, ss) =>
      for {
        x <- ec._toScala(s)
        xs <- listCsvLinesConverter(ss)(ec)
      } yield Cons(x, xs)
  }

  implicit def listCsvConverter[A <: Product](implicit ec: Scalable[A]): Scalable[List[A]] = new Scalable[List[A]] {
    override def _toScala(line: String): Option[List[A]] = listCsvLinesConverter[A](line.split(LINE_SEPARATOR).toList)(ec)

    override def toScala: Option[List[A]] = None
  }

}
