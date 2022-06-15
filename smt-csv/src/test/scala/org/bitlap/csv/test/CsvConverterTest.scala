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
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** @author
 *    梦境迷离
 *  @version 1.0,2022/4/29
 */
class CsvConverterTest extends AnyFlatSpec with Matchers {

  "CsvConverter1" should "ok" in {
    val line      = "abc,cdf,d,12,2,false,0.1,0.23333"
    val dimension = Converter[Dimension].toScala(line)
    assert(dimension.toString == "Some(Dimension(abc,Some(cdf),d,12,2,false,0.1,0.23333))")
    val csv = Converter[Dimension].toCsvString(dimension.orNull)
    println(csv)
    assert(csv == line)
  }

  "CsvConverter2" should "ok when csv column empty" in {
    val line =
      "abc,,d,12,2,false,0.1,0.23333"
    val dimension = Converter[Dimension].toScala(line)
    println(dimension.toString)
    assert(dimension.toString == "Some(Dimension(abc,None,d,12,2,false,0.1,0.23333))")
    val csv = Converter[Dimension].toCsvString(dimension.orNull)
    println(csv)
    assert(csv == line)

  }

  "CsvConverter3" should "failed when case class currying" in {
    """
      | case class Dimension(key: String, value: Option[String], d: Char, c: Long, e: Short, f: Boolean, g: Float)(h: Double)
      |    object Dimension {
      |       implicit val dimensionCsvConverter: Converter[Dimension] = new Converter[Dimension] {
      |          override def toScala(line: String): Option[Dimension] = DeriveToCaseClass[Dimension](line)
      |          override def toCsvString(t: Dimension): String = DeriveToString[Dimension](t)
      |       }
      |    }
      |""".stripMargin shouldNot compile
  }

  "CsvConverter4" should "ok when using list" in {
    val line =
      """1,cdf,d,12,2,false,0.1,0.2
        |2,cdf,d,12,2,false,0.1,0.1""".stripMargin
    val dimension = Converter[List[Dimension]].toScala(line)
    assert(
      dimension.toString == "Some(List(Dimension(1,Some(cdf),d,12,2,false,0.1,0.2), Dimension(2,Some(cdf),d,12,2,false,0.1,0.1)))"
    )
    val csv = Converter[List[Dimension]].toCsvString(dimension.orNull)
    println(csv)
    assert(csv == line)

  }

  "CsvConverter5" should "ok when input empty" in {
    val empty1 = Converter[List[Dimension]].toCsvString(Nil)
    println(empty1)
    assert(empty1 == "")

    val empty2 = Converter[List[Dimension]].toCsvString(null)
    println(empty2)
    assert(empty2 == "")
  }

  "CsvConverter6" should "ok when using json value" in {
    val line      = """abc,"{""a"":""b"",""c"":""d""}",d,12,2,false,0.1,0.23333"""
    val dimension = Converter[Dimension].toScala(line)
    println(dimension)
    assert(dimension.toString == "Some(Dimension(abc,Some({\"a\":\"b\",\"c\":\"d\"}),d,12,2,false,0.1,0.23333))")
  }

  "CsvConverter7" should "get None when error" in {
    // xxx should be Boolean, but failure, can get false
    val line      = """abc,"{""a"":""b"",""c"":""d""}",d,12,2,xxx,0.1,0.23333"""
    val dimension = Converter[Dimension].toScala(line)
    println(dimension)
    assert(dimension.toString == "Some(Dimension(abc,Some({\"a\":\"b\",\"c\":\"d\"}),d,12,2,false,0.1,0.23333))")
  }
}
