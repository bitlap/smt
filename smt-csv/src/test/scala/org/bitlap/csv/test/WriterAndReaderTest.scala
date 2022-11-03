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
import org.bitlap.csv._
import java.io.File
import org.bitlap.csv.FileUtils

/** Complex use of common tests
 *
 *  @author
 *    梦境迷离
 *  @version 1.0,2022/5/1
 */
class WriterAndReaderTest extends AnyFlatSpec with Matchers {

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

  "WriterAndReaderTest1" should "ok" in {
    val metrics = csvData
      .split("\n")
      .toList
      .map(csv =>
        ReaderBuilder[Metric]
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
      WriterBuilder[Metric]
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

  "WriterAndReaderTest2" should "ok" in {
    val metrics = csvData
      .split("\n")
      .toList
      .map(csv =>
        ReaderBuilder[Metric2]
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

  "WriterAndReaderTest3" should "ok when using StringUtils" in {
    val metrics = csvData
      .split("\n")
      .map(csv =>
        ReaderBuilder[Metric2]
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

  "WriterAndReaderTest4" should "ok when reading from file" in {
    val metrics = FileUtils.readCsvFromClassPath[Metric2]("simple_data.csv") { line =>
      ReaderBuilder[Metric2]
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

  "WriterAndReaderTest5" should "ok when using convert method" in {

    val csvLines = csvData.split("\n").toList

    val metrics = ReaderBuilder[Metric3].convert(csvLines)

    // if we don't define a custom function for convert `Metric3#dimension`
    val csv = WriterBuilder[Metric3].convert(metrics.filter(_.isDefined).map(_.get))

    println(metrics)
    println(csv)

    assert(csvData.replace("\"", "") == csv.replace("\"", ""))
  }

  "WriterAndReaderTest6" should "ok when using convert and StringUtils" in {
    val metrics = ReaderBuilder[Metric2]
      .setField[Seq[Dimension3]](
        _.dimensions,
        dims => StringUtils.extractJsonValues[Dimension3](dims)((k, v) => Dimension3(k, v))
      )
      .convert(csvData.split("\n").toList)

    metrics.foreach(println)

    assert(metrics.head.get.dimensions.head.key == "city")
    assert(metrics.head.get.dimensions.head.value == "北京")

    val csv = WriterBuilder[Metric2]
      .setField(
        _.dimensions,
        (ds: Seq[Dimension3]) =>
          s"""\"{${ds.map(kv => s"""\"\"${kv.key}\"\":\"\"${kv.value}\"\"""").mkString(",")}}\""""
      )
      .convert(metrics.filter(_.isDefined).map(_.get))

    println(csv)
  }

  "WriterAndReaderTest7" should "ok when reading from file" in {
    val metrics =
      ReaderBuilder[Metric2]
        .setField[Seq[Dimension3]](
          _.dimensions,
          dims => StringUtils.extractJsonValues[Dimension3](dims)((k, v) => Dimension3(k, v))
        )
        .convertFrom(ClassLoader.getSystemResourceAsStream("simple_data.csv"))

    println(metrics)
    assert(metrics.nonEmpty)

    val file = new File("./simple_data.csv")
    WriterBuilder[Metric2]
      .setField[Seq[Dimension3]](
        _.dimensions,
        ds => s"""\"{${ds.map(kv => s"""\"\"${kv.key}\"\":\"\"${kv.value}\"\"""").mkString(",")}}\""""
      )
      .convertTo(metrics.filter(_.isDefined).map(_.get), file)

    file.delete()
  }

  "WriterAndReaderTest8" should "ok when use custom format" in {
    implicit val format = new DefaultCsvFormat {
      override val ignoreEmptyLines: Boolean   = true
      override val ignoreHeader: Boolean       = true
      override val prependHeader: List[String] = List("time", "entity", "dimensions", "metricName", "metricValue")
    }
    val metrics =
      ReaderBuilder[Metric2]
        .setField[Seq[Dimension3]](
          _.dimensions,
          dims => StringUtils.extractJsonValues[Dimension3](dims)((k, v) => Dimension3(k, v))
        )
        .convertFrom(ClassLoader.getSystemResourceAsStream("simple_data_header.csv"))

    println(metrics)
    assert(metrics.nonEmpty)

    val file = new File("./simple_data_header.csv")
    WriterBuilder[Metric2]
      .setField[Seq[Dimension3]](
        _.dimensions,
        ds => s"""\"{${ds.map(kv => s"""\"\"${kv.key}\"\":\"\"${kv.value}\"\"""").mkString(",")}}\""""
      ) // NOTE: not support pass anonymous object to convertTo method.
      .convertTo(metrics.filter(_.isDefined).map(_.get), file)
    file.delete()
  }

  "WriterAndReaderTest9" should "failure if not setField" in {
    """
      |val metrics = ReaderBuilder[Metric].convert(csvData.split("\n").toList)
      |val csv = WriterBuilder[Metric].convert(metrics.filter(_.isDefined).map(_.get))
      |
      |val metrics2 = ReaderBuilder[Metric2].convert(csvData.split("\n").toList)
      |val csv2 = WriterBuilder[Metric2].convert(metrics2.filter(_.isDefined).map(_.get))
      |
      |
      |val metrics3 = ReaderBuilder[Metric4].convert(csvData.split("\n").toList)
      |val csv3 = WriterBuilder[Metric4].convert(metrics3.filter(_.isDefined).map(_.get))
      |
      |val metrics4 = ReaderBuilder[Metric5].convert(csvData.split("\n").toList)
      |val csv4 = WriterBuilder[Metric5].convert(metrics4.filter(_.isDefined).map(_.get))
      |""".stripMargin shouldNot compile
  }

}
