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

import scala.util.Try

/** @author
 *    梦境迷离
 *  @version 1.0,2022/5/1
 */
trait ScalableImplicits {

  implicit val stringScalable: Scalable[String] = new Scalable[String] {
    override def _toScala(column: String): Option[String] = if (column.isEmpty) None else Some(column)
  }

  implicit val intScalable: Scalable[Int] = new Scalable[Int] {
    override def _toScala(column: String): Option[Int] = Try(column.toInt).toOption
  }

  implicit val charScalable: Scalable[Char] = new Scalable[Char] {
    override def _toScala(column: String): Option[Char] = if (column.isEmpty) None else Try(column.charAt(0)).toOption
  }

  implicit val longScalable: Scalable[Long] = new Scalable[Long] {
    override def _toScala(column: String): Option[Long] = Try(column.toLong).toOption
  }

  implicit val shortScalable: Scalable[Short] = new Scalable[Short] {
    override def _toScala(column: String): Option[Short] = Try(column.toShort).toOption
  }

  implicit val doubleScalable: Scalable[Double] = new Scalable[Double] {
    override def _toScala(column: String): Option[Double] = Try(column.toDouble).toOption
  }

  implicit val floatScalable: Scalable[Float] = new Scalable[Float] {
    override def _toScala(column: String): Option[Float] = Try(column.toFloat).toOption
  }

  implicit val booleanScalable: Scalable[Boolean] = new Scalable[Boolean] {
    override def _toScala(column: String): Option[Boolean] = Try(column.toBoolean).toOption
  }
}
