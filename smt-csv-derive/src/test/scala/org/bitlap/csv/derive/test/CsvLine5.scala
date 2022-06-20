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

package org.bitlap.csv.derive.test

import org.bitlap.csv.Converter
import org.bitlap.csv.derive.DeriveCsvConverter

/** @author
 *    梦境迷离
 *  @version 1.0,2022/4/29
 */
case class CsvLine5(key: String, values: Vector[String], elements: Set[String])

object CsvLine5 {

  implicit val setString: Converter[scala.Predef.Set[String]] = new Converter[Set[String]] {
    override def toScala(line: String) = Option(Set(line))

    override def toCsvString(t: Set[String]) = t.head
  }

  implicit val vectorString: Converter[scala.Vector[String]] = new Converter[Vector[String]] {
    override def toScala(line: String) = Option(Vector(line))

    override def toCsvString(t: Vector[String]) = t.head
  }

  implicit val lineCsvConverter: Converter[CsvLine5] = DeriveCsvConverter.gen[CsvLine5]
}
