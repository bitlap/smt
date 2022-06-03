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
import org.bitlap.csv.core.DefaultCsvFormat

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.bitlap.csv.core.ScalableBuilder
import org.bitlap.csv.core.CsvableBuilder
import org.bitlap.csv.core.ScalableHelper
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

  "CsvableAndScalable7" should "ok macro expose code" in {
    // TO SCALA
    // These code are almost identical to the macro expansion code. Only some modification, in order to compile in the test
    val metrics: List[Option[Metric2]] = Nil
    lazy val _ScalableBuilderFunction$dimensions: String => List[org.bitlap.csv.core.test.Dimension3] =
      (dims: String) =>
        org.bitlap.csv.core.StringUtils.extractJsonValues[org.bitlap.csv.core.test.Dimension3](dims)(
          (k: String, v: String) => Dimension3.apply(k, v)
        );
    object _ScalaAnno$10 extends _root_.org.bitlap.csv.core.Scalable[Metric2] {
      var _line: String = _;
      private val _columns = () =>
        _root_.org.bitlap.csv.core.StringUtils.splitColumns(_ScalaAnno$10._line, org.bitlap.csv.core.defaultCsvFormat);

      override def _toScala(column: String): Option[Metric2] = Option(
        Metric2(
          _root_.org.bitlap.csv.core.Scalable[Long]._toScala(_columns()(0)).getOrElse(0L),
          _root_.org.bitlap.csv.core.Scalable[Int]._toScala(_columns()(1)).getOrElse(0),
          _ScalableBuilderFunction$dimensions
            .apply(_columns()(2))
            .asInstanceOf[Seq[org.bitlap.csv.core.test.Dimension3]],
          _root_.org.bitlap.csv.core.Scalable[String]._toScala(_columns()(3)).getOrElse(""),
          _root_.org.bitlap.csv.core.Scalable[Int]._toScala(_columns()(4)).getOrElse(0)
        )
      )
    };
    lazy val _scalableInstance = _ScalaAnno$10;
    _root_.org.bitlap.csv.core.FileUtils
      .reader(java.lang.ClassLoader.getSystemResourceAsStream("simple_data.csv"), org.bitlap.csv.core.defaultCsvFormat)
      .map { (_l: String) =>
        _scalableInstance._line = _l;
        _scalableInstance._toScala(_l)
      }

    // TO CSV
    val file = new File("./simple_data.csv")

    lazy val _CsvableBuilderFunction$dimensions: Seq[org.bitlap.csv.core.test.Dimension3] => String =
      (ds: Seq[org.bitlap.csv.core.test.Dimension3]) =>
        ("\"{"
          .+(
            ds.map[String](
              (
                (kv: org.bitlap.csv.core.test.Dimension3) =>
                  ("\"\"".+(kv.key).+("\"\":\"\"").+(kv.value).+("\"\""): String)
              )
            ).mkString(",")
          )
          .+("}\""): String);
    object _CsvAnno$11 extends _root_.org.bitlap.csv.core.Csvable[Metric2] {
      var _tt: Metric2 = _;
      lazy private val toCsv = (temp: Metric2) => {
        val fields = Metric2.unapply(temp).orNull;
        if (null.$eq$eq(fields))
          ""
        else
          scala.collection.immutable
            .List(
              _root_.org.bitlap.csv.core.Csvable[Long]._toCsvString(temp.time),
              _root_.org.bitlap.csv.core.Csvable[Int]._toCsvString(temp.entity),
              _CsvableBuilderFunction$dimensions.apply(temp.dimensions),
              _root_.org.bitlap.csv.core.Csvable[String]._toCsvString(temp.metricName),
              _root_.org.bitlap.csv.core.Csvable[Int]._toCsvString(temp.metricValue)
            )
            .mkString(org.bitlap.csv.core.defaultCsvFormat.delimiter.toString)
      };

      override def _toCsvString(t: Metric2): String = toCsv(_CsvAnno$11._tt)
    };
    lazy val _csvableInstance = _CsvAnno$11;
    _root_.org.bitlap.csv.core.FileUtils.writer(
      file,
      metrics
        .filter((x$18: Option[org.bitlap.csv.core.test.Metric2]) => x$18.isDefined)
        .map[org.bitlap.csv.core.test.Metric2]((x$19: Option[org.bitlap.csv.core.test.Metric2]) => x$19.get)
        .map { (_t: Metric2) =>
          _csvableInstance._tt = _t;
          _csvableInstance._toCsvString(_t)
        },
      org.bitlap.csv.core.defaultCsvFormat
    )
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
      override val ignoreEmptyLines: Boolean = true
      override val ignoreHeader: Boolean     = true
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
    // NOTE: not support pass anonymous objects to convertTo method.
    val format2 = new DefaultCsvFormat {
      override val ignoreEmptyLines: Boolean = true
      override val ignoreHeader: Boolean     = true
      override val headerRow: List[String]   = List("time", "entity", "dimensions", "metricName", "metricValue")
    }
    CsvableBuilder[Metric2]
      .setField[Seq[Dimension3]](
        _.dimensions,
        ds => s"""\"{${ds.map(kv => s"""\"\"${kv.key}\"\":\"\"${kv.value}\"\"""").mkString(",")}}\""""
      )
      .convertTo(metrics.filter(_.isDefined).map(_.get), file)(format2)

    //    file.delete()
  }

}
