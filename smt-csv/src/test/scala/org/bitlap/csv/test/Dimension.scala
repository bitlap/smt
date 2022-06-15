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

package org.bitlap.csv.test

import org.bitlap.csv.Converter
import org.bitlap.csv.macros.{ DeriveToCaseClass, DeriveToString }

/** @author
 *    梦境迷离
 *  @version 1.0,2022/4/29
 */
case class Dimension(key: String, value: Option[String], d: Char, c: Long, e: Short, f: Boolean, g: Float, h: Double)

// do not have a implicit val in companion Object
case class Dimension2(key: String, value: Option[String], d: Char, c: Long, e: Short, f: Boolean, g: Float, h: Double)

case class Metric(time: Long, entity: Int, dimensions: List[Dimension3], metricName: String, metricValue: Int)

case class Metric2(time: Long, entity: Int, dimensions: Seq[Dimension3], metricName: String, metricValue: Int)

case class Metric3(time: Long, entity: Int, dimensions: String, metricName: String, metricValue: Int)

case class Dimension3(key: String, value: String)

object Dimension {

  implicit def dimensionCsvConverter: Converter[Dimension] = new Converter[Dimension] {

    override def toScala(line: String): Option[Dimension] = DeriveToCaseClass[Dimension](line)

    override def toCsvString(t: Dimension): String = DeriveToString[Dimension](t)
  }
}
