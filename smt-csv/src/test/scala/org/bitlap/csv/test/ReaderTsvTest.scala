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

import org.bitlap.csv.{ ReaderBuilder, TsvFormat, WriterBuilder }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.File

/** @author
 *    梦境迷离
 *  @version 1.0,6/4/22
 */
class ReaderTsvTest extends AnyFlatSpec with Matchers {

  "ScalableTsvTest1" should "ok when file is tsv" in {
    implicit val format = new TsvFormat {
      override val delimiter: Char             = ' '
      override val ignoreEmptyLines: Boolean   = true
      override val ignoreHeader: Boolean       = true
      override val prependHeader: List[String] = List("time", "entity", "dimensions", "metricName", "metricValue")
    }
    val metrics =
      ReaderBuilder[Metric3]
        .convertFrom(ClassLoader.getSystemResourceAsStream("simple_data_header.tsv"))
    println(metrics)
    assert(metrics.nonEmpty)
    assert(
      metrics.toString() ==
        """List(Some(Metric3(100,1,{"city":"北京","os":"Mac"},vv,1)), Some(Metric3(100,1,{"city":"北京","os":"Mac"},pv,2)), Some(Metric3(100,1,{"city":"北京","os":"Windows"},vv,1)), Some(Metric3(100,1,{"city":"北京","os":"Windows"},pv,3)))"""
    )

    val file = new File("./simple_data_header.tsv")
    WriterBuilder[Metric3]
      // NOTE: not support pass anonymous object to convertTo method.
      .convertTo(metrics.filter(_.isDefined).map(_.get), file)
    file.delete()
  }
}
