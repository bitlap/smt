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

import org.bitlap.csv.core.{ Converter, StringUtils }
import org.bitlap.csv.derive.DeriveCsvConverter

/**
 * @author 梦境迷离
 * @version 1.0,2022/5/15
 */
case class CsvLine4(time: Long, entity: Int, dimensions: List[Dimension], metricName: String, metricValue: Int)

case class Dimension(key: String, value: String)

object Dimension {

  implicit val fieldCsvConverter: Converter[List[Dimension]] = new Converter[List[Dimension]] {
    override def toScala(line: String): Option[List[Dimension]] =
      Option(StringUtils.extractJsonValues[Dimension](line)((k, v) => Dimension(k, v)))

    override def toCsvString(t: List[Dimension]): String =
      s"""\"{${t.map(kv => s"""\"\"${kv.key}\"\":\"\"${kv.value}\"\"""").mkString(",")}}\""""
  }

}

object CsvLine4 {

  implicit val lineCsvConverter: Converter[CsvLine4] = DeriveCsvConverter.gen[CsvLine4]

}
