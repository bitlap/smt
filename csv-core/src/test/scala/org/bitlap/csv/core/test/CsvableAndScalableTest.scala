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

package org.bitlap.csv.core.test

import org.bitlap.csv.core.StringUtils

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.bitlap.csv.core.ScalableBuilder
import org.bitlap.csv.core.CsvableBuilder

/**
 * Complex use of common tests
 *
 * @author 梦境迷离
 * @version 1.0,2022/5/1
 */
class CsvableAndScalableTest extends AnyFlatSpec with Matchers {

  val csvData =
    """100,1,"{""city"":""北京"",""os"":""Mac""}",vv,1
      |100,1,"{""city"":""北京"",""os"":""Mac""}",pv,2
      |100,1,"{""city"":""北京"",""os"":""Windows""}",vv,1
      |100,1,"{""city"":""北京"",""os"":""Windows""}",pv,3
      |100,2,"{""city"":""北京"",""os"":""Mac""}",vv,1
      |100,2,"{""city"":""北京"",""os"":""Mac""}",pv,5
      |100,3,"{""city"":""北京"",""os"":""Mac""}",vv,1
      |100,3,"{""city"":""北京"",""os"":""Mac""}",pv,2
      |200,1,"{""city"":""北京"",""os"":""Mac""}",vv,1
      |200,1,"{""city"":""北京"",""os"":""Mac""}",pv,2
      |200,1,"{""city"":""北京"",""os"":""Windows""}",vv,1
      |200,1,"{""city"":""北京"",""os"":""Windows""}",pv,3
      |200,2,"{""city"":""北京"",""os"":""Mac""}",vv,1
      |200,2,"{""city"":""北京"",""os"":""Mac""}",pv,5
      |200,3,"{""city"":""北京"",""os"":""Mac""}",vv,1
      |200,3,"{""city"":""北京"",""os"":""Mac""}",pv,2""".stripMargin

  "CsvableAndScalable1" should "ok" in {
    val metrics = csvData
      .split("\n")
      .toList
      .map(csv =>
        ScalableBuilder[Metric]
          .setField(
            _.dimensions,
            dims => {
              val jsonElements = dims
                .replace("\"", "")
                .split(",")
                .map(_.replace("{", "").replace("}", ""))
              println(jsonElements.toList)
              val kvs = jsonElements.map(f => f.split(":")(0) -> f.split(":")(1))
              kvs.map(kv => Dimension3(kv._1, kv._2)).toList
            }
          )
          .build(csv, ',')
          .toScala
      )

    println(metrics)

    assert(metrics.head.get.dimensions.head.key == "city")
    assert(metrics.head.get.dimensions.head.value == "北京")

    val csv = metrics.map(metric =>
      CsvableBuilder[Metric]
        .setField(
          _.dimensions,
          (ds: List[Dimension3]) => s"{${ds.map(kv => s"""\"\"${kv.key}\"\":\"\"${kv.value}\"\"""").mkString(",")}"
        )
        .build(metric.get, ',')
        .toCsvString
    )

    println(csv)
    assert(
      csv.toString() == """List(100,1,{""city"":""北京"",""os"":""Mac"",vv,1, 100,1,{""city"":""北京"",""os"":""Mac"",pv,2, 100,1,{""city"":""北京"",""os"":""Windows"",vv,1, 100,1,{""city"":""北京"",""os"":""Windows"",pv,3, 100,2,{""city"":""北京"",""os"":""Mac"",vv,1, 100,2,{""city"":""北京"",""os"":""Mac"",pv,5, 100,3,{""city"":""北京"",""os"":""Mac"",vv,1, 100,3,{""city"":""北京"",""os"":""Mac"",pv,2, 200,1,{""city"":""北京"",""os"":""Mac"",vv,1, 200,1,{""city"":""北京"",""os"":""Mac"",pv,2, 200,1,{""city"":""北京"",""os"":""Windows"",vv,1, 200,1,{""city"":""北京"",""os"":""Windows"",pv,3, 200,2,{""city"":""北京"",""os"":""Mac"",vv,1, 200,2,{""city"":""北京"",""os"":""Mac"",pv,5, 200,3,{""city"":""北京"",""os"":""Mac"",vv,1, 200,3,{""city"":""北京"",""os"":""Mac"",pv,2)"""
    )
  }

  "CsvableAndScalable2" should "ok" in {
    val metrics = csvData
      .split("\n")
      .toList
      .map(csv =>
        ScalableBuilder[Metric2]
          .setField[Seq[Dimension3]](
            _.dimensions,
            dims => {
              val jsonElements = dims
                .replace("\"", "")
                .split(",")
                .map(_.replace("{", "").replace("}", ""))
              println(jsonElements.toList)
              val kvs = jsonElements.map(f => f.split(":")(0) -> f.split(":")(1))
              kvs.map(kv => Dimension3(kv._1, kv._2)).toSeq
            }
          )
          .build(csv, ',')
          .toScala
      )

    println(metrics)

    assert(metrics.head.get.dimensions.head.key == "city")
    assert(metrics.head.get.dimensions.head.value == "北京")
  }

  "CsvableAndScalable3" should "ok when using StringUtils" in {
    val metrics = csvData
      .split("\n")
      .map(csv =>
        ScalableBuilder[Metric2]
          .setField[Seq[Dimension3]](
            _.dimensions,
            dims => StringUtils.extractJsonValues[Dimension3](dims)((k, v) => Dimension3(k, v))
          )
          .build(csv)
          .toScala
      )

    println(metrics.toList)

    assert(metrics.head.get.dimensions.head.key == "city")
    assert(metrics.head.get.dimensions.head.value == "北京")
  }
}
