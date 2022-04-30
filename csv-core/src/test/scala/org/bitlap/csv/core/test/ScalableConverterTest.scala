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

import org.bitlap.csv.core.{ Converter, ScalableBuilder }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 *
 * @author 梦境迷离
 * @version 1.0,2022/4/29
 */
class ScalableConverterTest extends AnyFlatSpec with Matchers {

  "ScalableConverter1" should "ok" in {
    val line = "abc,cdf,d,12,2,false,0.1,0.23333"
    val dimension = ScalableBuilder[Dimension].build(line, ',').toScala
    assert(dimension.toString == "Some(Dimension(abc,Some(cdf),d,12,2,false,0.1,0.23333))")

    val csv = Converter[Dimension].toCsvString(dimension.orNull)
    println(csv)
    assert(csv == line)
  }

  "ScalableConverter2" should "ok when using json value" in {
    val line = """abc,"{""a"":""b"",""c"":""d""}",d,12,2,false,0.1,0.23333"""
    val dimension = ScalableBuilder[Dimension]
      .setField(_.value, s => Some("helloworld"))
      .build(line, ',')
      .toScala
    println(dimension)
    assert(dimension.toString == "Some(Dimension(abc,Some(helloworld),d,12,2,false,0.1,0.23333))")
  }

  "ScalableConverter3" should "ok when using json value" in {
    val line = """abc,"{""a"":""b"",""c"":""d""}",d,12,2,false,0.1,0.23333"""
    val dimension = ScalableBuilder[Dimension]
      .setField(_.value, s => None)
      .build(line, ',')
      .toScala

    println(dimension)
    assert(dimension.toString == "Some(Dimension(abc,None,d,12,2,false,0.1,0.23333))")
  }

  "ScalableConverter4" should "ok when using json value" in {
    val line = """abc,"{""a"":""b"",""c"":""d""}",d,12,2,false,0.1,0.23333"""
    val d = ScalableBuilder[Dimension]
      .setField(_.value, s => None)
      .build(line, ',')
      .toScala
    assert(d.toString == "Some(Dimension(abc,None,d,12,2,false,0.1,0.23333))")

    val e = ScalableBuilder[Dimension]
      .build(line, ',')
      .toScala
    println(e)
    assert(e.toString == "Some(Dimension(abc,Some({\"a\":\"b\",\"c\":\"d\"}),d,12,2,false,0.1,0.23333))")

    // not work
    //    val dimension = CsvableBuilder[Dimension]
    //      .build(',').toCsvString(d.orNull)
    //        assert(dimension == "Some(Dimension(abc,None,d,12,2,false,0.1,0.23333))")
  }
}
