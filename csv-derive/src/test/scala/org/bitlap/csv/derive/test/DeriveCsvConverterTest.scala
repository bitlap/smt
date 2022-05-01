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

import org.bitlap.csv.core.Converter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * @author 梦境迷离
 * @version 1.0,2022/4/29
 */
class DeriveCsvConverterTest extends AnyFlatSpec with Matchers {

  "DeriveCsvConverter1" should "ok" in {
    val line = "abc,cdf,d,12,2,false,0.1,0.23333"
    val dimension = Converter[Dimension].toScala(line)
    assert(dimension.toString == "Some(Dimension(abc,Some(cdf),d,12,2,false,0.1,0.23333))")
    val csv = Converter[Dimension].toCsvString(dimension.orNull)
    println(csv)
    assert(csv == line)
  }

  "DeriveCsvConverter2" should "ok when csv column empty" in {
    val line =
      "abc,,d,12,2,false,0.1,0.23333"
    val dimension = Converter[Dimension].toScala(line)
    println(dimension.toString)
    assert(dimension.toString == "Some(Dimension(abc,None,d,12,2,false,0.1,0.23333))")
  }

  "DeriveCsvConverter3" should "ok when using list" in {
    val line =
      """1,cdf,d,12,2,false,0.1,0.2
        |2,cdf,d,12,2,false,0.1,0.1""".stripMargin
    val dimension = Converter[List[Dimension]].toScala(line)
    assert(
      dimension.toString == "Some(List(Dimension(1,Some(cdf),d,12,2,false,0.1,0.2), Dimension(2,Some(cdf),d,12,2,false,0.1,0.1)))"
    )

  }

  "DeriveCsvConverter4" should "ok when using custom columnSeparator" in {
    val line =
      """1 22
        |2 0.1""".stripMargin
    val dimension = Converter[List[Dimension2]].toScala(line)
    assert(dimension.toString == "Some(List(Dimension2(1,Some(22)), Dimension2(2,Some(0.1))))")
    val csv = Converter[List[Dimension2]].toCsvString(dimension.orNull)
    println(csv)
    assert(csv == line)

  }

}
