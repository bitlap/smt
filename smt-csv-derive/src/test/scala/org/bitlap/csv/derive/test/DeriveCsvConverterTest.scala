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
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** @author
 *    梦境迷离
 *  @version 1.0,2022/4/29
 */
class DeriveCsvConverterTest extends AnyFlatSpec with Matchers {

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

  "DeriveCsvConverter1" should "ok" in {
    val line      = "abc,cdf,d,12,2,false,0.1,0.23333"
    val dimension = Converter[CsvLine].toScala(line)
    assert(dimension.toString == "Some(CsvLine(abc,Some(cdf),d,12,2,false,0.1,0.23333))")
    val csv = Converter[CsvLine].toCsvString(dimension.orNull)
    assert(csv == line)
  }

  "DeriveCsvConverter2" should "ok when csv column empty" in {
    val line =
      "abc,,d,12,2,false,0.1,0.23333"
    val dimension = Converter[CsvLine].toScala(line)
    assert(dimension.toString == "Some(CsvLine(abc,None,d,12,2,false,0.1,0.23333))")
  }

  "DeriveCsvConverter3" should "ok when using list" in {
    val line =
      """1,cdf,d,12,2,false,0.1,0.2
        |2,cdf,d,12,2,false,0.1,0.1""".stripMargin
    val dimension = Converter[List[CsvLine]].toScala(line)
    assert(
      dimension.toString == "Some(List(CsvLine(1,Some(cdf),d,12,2,false,0.1,0.2), CsvLine(2,Some(cdf),d,12,2,false,0.1,0.1)))"
    )

  }

  "DeriveCsvConverter4" should "ok when using custom columnSeparator" in {
    val line =
      """1 22
        |2 0.1""".stripMargin
    val dimension = Converter[List[CsvLine2]].toScala(line)
    assert(dimension.toString == "Some(List(CsvLine2(1,Some(22)), CsvLine2(2,Some(0.1))))")
    val csv = Converter[List[CsvLine2]].toCsvString(dimension.orNull)
    assert(csv == line)

  }

  "DeriveCsvConverter5" should "ok when using custom Converter" in {
    val line =
      """1,2022-02-12 08:00:00
        |2,2022-02-12 08:00:00""".stripMargin
    val dimension = Converter[List[CsvLine3]].toScala(line)
    println(dimension.toString)
    assert(dimension.toString == "Some(List(CsvLine3(1,2022-02-12T08:00), CsvLine3(2,2022-02-12T08:00)))")
    val csv = Converter[List[CsvLine3]].toCsvString(dimension.orNull)
    assert(csv == line)
    println(csv)
  }

  "DeriveCsvConverter6" should "ok when using custom Converter for case class" in {
    val csvs = Converter[List[CsvLine4]].toScala(csvData)
    println(csvs.toString)
    assert(
      csvs.toString == "Some(List(CsvLine4(100,1,List(Dimension(city,北京), Dimension(os,Mac)),vv,1), CsvLine4(100,1,List(Dimension(city,北京), Dimension(os,Mac)),pv,2), CsvLine4(100,1,List(Dimension(city,北京), Dimension(os,Windows)),vv,1), CsvLine4(100,1,List(Dimension(city,北京), Dimension(os,Windows)),pv,3), CsvLine4(100,2,List(Dimension(city,北京), Dimension(os,Mac)),vv,1), CsvLine4(100,2,List(Dimension(city,北京), Dimension(os,Mac)),pv,5), CsvLine4(100,3,List(Dimension(city,北京), Dimension(os,Mac)),vv,1), CsvLine4(100,3,List(Dimension(city,北京), Dimension(os,Mac)),pv,2), CsvLine4(200,1,List(Dimension(city,北京), Dimension(os,Mac)),vv,1), CsvLine4(200,1,List(Dimension(city,北京), Dimension(os,Mac)),pv,2), CsvLine4(200,1,List(Dimension(city,北京), Dimension(os,Windows)),vv,1), CsvLine4(200,1,List(Dimension(city,北京), Dimension(os,Windows)),pv,3), CsvLine4(200,2,List(Dimension(city,北京), Dimension(os,Mac)),vv,1), CsvLine4(200,2,List(Dimension(city,北京), Dimension(os,Mac)),pv,5), CsvLine4(200,3,List(Dimension(city,北京), Dimension(os,Mac)),vv,1), CsvLine4(200,3,List(Dimension(city,北京), Dimension(os,Mac)),pv,2)))"
    )
    val csv = Converter[List[CsvLine4]].toCsvString(csvs.orNull)
    assert(csv == csvData)
    println(csv)
  }

  "DeriveCsvConverter7 when field is vector or set" should "ok" in {
    val line      = "abc,cdf,d"
    val dimension = Converter[CsvLine5].toScala(line)
    println(dimension)
    assert(dimension.toString == "Some(CsvLine5(abc,Vector(cdf),Set(d)))")
    val csv = Converter[CsvLine5].toCsvString(dimension.orNull)
    println(csv)
    assert(csv == line)
  }
}
