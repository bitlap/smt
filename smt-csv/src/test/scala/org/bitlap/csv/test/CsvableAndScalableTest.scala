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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.bitlap.csv.{ CsvableBuilder, DefaultCsvFormat, ScalableBuilder, ScalableHelper, StringUtils }
import java.io.File

/** Complex use of common tests
 *
 *  @author
 *    梦境迷离
 *  @version 1.0,2022/5/1
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
          .convert(csv)
      )

    println(metrics)

    assert(metrics.head.get.dimensions.head.key == "city")
    assert(metrics.head.get.dimensions.head.value == "北京")

    val csv = metrics.map(metric =>
      CsvableBuilder[Metric]
        .setField(
          _.dimensions,
          (ds: List[Dimension3]) =>
            s"""\"{${ds.map(kv => s"""\"\"${kv.key}\"\":\"\"${kv.value}\"\"""").mkString(",")}}\""""
        )
        .convert(metric.get)
    )

    println(csv)
    assert(
      csv.toString() == """List(100,1,"{""city"":""北京"",""os"":""Mac""}",vv,1, 100,1,"{""city"":""北京"",""os"":""Mac""}",pv,2, 100,1,"{""city"":""北京"",""os"":""Windows""}",vv,1, 100,1,"{""city"":""北京"",""os"":""Windows""}",pv,3, 100,2,"{""city"":""北京"",""os"":""Mac""}",vv,1, 100,2,"{""city"":""北京"",""os"":""Mac""}",pv,5, 100,3,"{""city"":""北京"",""os"":""Mac""}",vv,1, 100,3,"{""city"":""北京"",""os"":""Mac""}",pv,2, 200,1,"{""city"":""北京"",""os"":""Mac""}",vv,1, 200,1,"{""city"":""北京"",""os"":""Mac""}",pv,2, 200,1,"{""city"":""北京"",""os"":""Windows""}",vv,1, 200,1,"{""city"":""北京"",""os"":""Windows""}",pv,3, 200,2,"{""city"":""北京"",""os"":""Mac""}",vv,1, 200,2,"{""city"":""北京"",""os"":""Mac""}",pv,5, 200,3,"{""city"":""北京"",""os"":""Mac""}",vv,1, 200,3,"{""city"":""北京"",""os"":""Mac""}",pv,2)"""
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
          .convert(csv)
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
          .convert(csv)
      )

    println(metrics.toList)

    assert(metrics.head.get.dimensions.head.key == "city")
    assert(metrics.head.get.dimensions.head.value == "北京")
  }

  "CsvableAndScalable4" should "ok when reading from file" in {
    import org.bitlap.csv.ScalableHelper
    val metrics = ScalableHelper.readCsvFromClassPath[Metric2]("simple_data.csv") { line =>
      ScalableBuilder[Metric2]
        .setField[Seq[Dimension3]](
          _.dimensions,
          dims => StringUtils.extractJsonValues[Dimension3](dims)((k, v) => Dimension3(k, v))
        )
        .convert(line)
    }

    println(metrics)
    assert(metrics.head.get.dimensions.head.key == "city")
    assert(metrics.head.get.dimensions.head.value == "北京")
  }

  "CsvableAndScalable5" should "ok when using convert method" in {

    val csvLines = csvData.split("\n").toList

    val metrics = ScalableBuilder[Metric3].convert(csvLines)

    // if we don't define a custom function for convert `Metric3#dimension`
    val csv = CsvableBuilder[Metric3].convert(metrics.filter(_.isDefined).map(_.get))

    println(metrics)
    println(csv)

    assert(csvData.replace("\"", "") == csv.replace("\"", ""))
  }

  "CsvableAndScalable6" should "ok when using convert and StringUtils" in {
    val metrics = ScalableBuilder[Metric2]
      .setField[Seq[Dimension3]](
        _.dimensions,
        dims => StringUtils.extractJsonValues[Dimension3](dims)((k, v) => Dimension3(k, v))
      )
      .convert(csvData.split("\n").toList)

    metrics.foreach(println)

    assert(metrics.head.get.dimensions.head.key == "city")
    assert(metrics.head.get.dimensions.head.value == "北京")

    val csv = CsvableBuilder[Metric2]
      .setField(
        _.dimensions,
        (ds: Seq[Dimension3]) =>
          s"""\"{${ds.map(kv => s"""\"\"${kv.key}\"\":\"\"${kv.value}\"\"""").mkString(",")}}\""""
      )
      .convert(metrics.filter(_.isDefined).map(_.get))

    println(csv)
  }

  "CsvableAndScalable8" should "ok when reading from file" in {
    val metrics =
      ScalableBuilder[Metric2]
        .setField[Seq[Dimension3]](
          _.dimensions,
          dims => StringUtils.extractJsonValues[Dimension3](dims)((k, v) => Dimension3(k, v))
        )
        .convertFrom(ClassLoader.getSystemResourceAsStream("simple_data.csv"))

    println(metrics)
    assert(metrics.nonEmpty)

    val file = new File("./simple_data.csv")
    CsvableBuilder[Metric2]
      .setField[Seq[Dimension3]](
        _.dimensions,
        ds => s"""\"{${ds.map(kv => s"""\"\"${kv.key}\"\":\"\"${kv.value}\"\"""").mkString(",")}}\""""
      )
      .convertTo(metrics.filter(_.isDefined).map(_.get), file)

    file.delete()
  }

  "CsvableAndScalable9" should "ok when use custom format" in {
    implicit val format = new DefaultCsvFormat {
      override val ignoreEmptyLines: Boolean   = true
      override val ignoreHeader: Boolean       = true
      override val prependHeader: List[String] = List("time", "entity", "dimensions", "metricName", "metricValue")
    }
    val metrics =
      ScalableBuilder[Metric2]
        .setField[Seq[Dimension3]](
          _.dimensions,
          dims => StringUtils.extractJsonValues[Dimension3](dims)((k, v) => Dimension3(k, v))
        )
        .convertFrom(ClassLoader.getSystemResourceAsStream("simple_data_header.csv"))

    println(metrics)
    assert(metrics.nonEmpty)

    val file = new File("./simple_data_header.csv")
    CsvableBuilder[Metric2]
      .setField[Seq[Dimension3]](
        _.dimensions,
        ds => s"""\"{${ds.map(kv => s"""\"\"${kv.key}\"\":\"\"${kv.value}\"\"""").mkString(",")}}\""""
      ) // NOTE: not support pass anonymous object to convertTo method.
      .convertTo(metrics.filter(_.isDefined).map(_.get), file)
    file.delete()
  }

  "CsvableAndScalable10" should "failure if not setField" in {
    """
      |val metrics = ScalableBuilder[Metric].convert(csvData.split("\n").toList)
      |val csv = CsvableBuilder[Metric].convert(metrics.filter(_.isDefined).map(_.get))
      |
      |val metrics2 = ScalableBuilder[Metric2].convert(csvData.split("\n").toList)
      |val csv2 = CsvableBuilder[Metric2].convert(metrics2.filter(_.isDefined).map(_.get))
      |
      |
      |val metrics3 = ScalableBuilder[Metric4].convert(csvData.split("\n").toList)
      |val csv3 = CsvableBuilder[Metric4].convert(metrics3.filter(_.isDefined).map(_.get))
      |
      |val metrics4 = ScalableBuilder[Metric5].convert(csvData.split("\n").toList)
      |val csv4 = CsvableBuilder[Metric5].convert(metrics4.filter(_.isDefined).map(_.get))
      |""".stripMargin shouldNot compile
  }

}
