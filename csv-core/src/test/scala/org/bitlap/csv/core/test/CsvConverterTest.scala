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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.bitlap.csv.core.CsvConverter

/**
 *
 * @author 梦境迷离
 * @version 1.0,2022/4/29
 */
class CsvConverterTest extends AnyFlatSpec with Matchers {

  "CsvConverter1" should "ok" in {
    val line = "abc,cdf,d,12,2,false,0.1,0.23333"
    val dimension = CsvConverter[Dimension].from(line)
    assert(dimension.toString == "Some(Dimension(abc,Some(cdf),d,12,2,false,0.1,0.23333))")

    val csv = CsvConverter[Dimension].to(dimension.orNull)
    println(csv)
    assert(csv == line)

  }

  "CsvConverter2" should "ok when csv column empty" in {
    val line =
      "abc,,d,12,2,false,0.1,0.23333"
    val dimension = CsvConverter[Dimension].from(line)
    println(dimension.toString)
    assert(dimension.toString == "Some(Dimension(abc,None,d,12,2,false,0.1,0.23333))")
    val csv = CsvConverter[Dimension].to(dimension.orNull)
    println(csv)
    assert(csv == line)

  }

  "CsvConverter3" should "failed when case class currying" in {
    """
      | case class Dimension(key: String, value: Option[String], d: Char, c: Long, e: Short, f: Boolean, g: Float)( h: Double)
      |    object Dimension {
      |      implicit def dimensionCsvConverter: CsvConverter[Dimension] = new CsvConverter[Dimension] {
      |        override def from(line: String): Option[Dimension] = Option(DeriveCaseClassBuilder[Dimension](line, ","))
      |        override def to(t: Dimension): String = DeriveStringBuilder[Dimension](t)
      |      }
      |
      |    }
      |""".stripMargin shouldNot compile
  }

  "CsvConverter4" should "ok when using list" in {
    val line =
      """1,cdf,d,12,2,false,0.1,0.2
        |2,cdf,d,12,2,false,0.1,0.1""".stripMargin
    val dimension = CsvConverter[List[Dimension]].from(line)
    assert(dimension.toString == "Some(List(Dimension(1,Some(cdf),d,12,2,false,0.1,0.2), Dimension(2,Some(cdf),d,12,2,false,0.1,0.1)))")
    val csv = CsvConverter[List[Dimension]].to(dimension.orNull)
    println(csv)
    assert(csv == line)

  }

  "CsvConverter5" should "ok when input empty" in {
    val empty1 = CsvConverter[List[Dimension]].to(Nil)
    println(empty1)
    assert(empty1 == "")

    val empty2 = CsvConverter[List[Dimension]].to(null)
    println(empty2)
    assert(empty2 == "")
  }
}
