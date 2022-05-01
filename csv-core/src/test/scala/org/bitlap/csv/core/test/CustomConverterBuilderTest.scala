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
import org.bitlap.csv.core.CsvableBuilder

/**
 *
 * @author 梦境迷离
 * @version 1.0,2022/4/29
 */
class CustomConverterBuilderTest extends AnyFlatSpec with Matchers {

  "CustomConverterBuilder1" should "ok" in {
    val line = "abc,cdf,d,12,2,false,0.1,0.23333"
    val dimension = ScalableBuilder[Dimension].build(line, ',').toScala
    assert(dimension.toString == "Some(Dimension(abc,Some(cdf),d,12,2,false,0.1,0.23333))")
    val csv = Converter[Dimension].toCsvString(dimension.orNull)
    println(csv)
    assert(csv == line)
  }

  "CustomConverterBuilder2" should "ok when using json value" in {
    val line = """abc,"{""a"":""b"",""c"":""d""}",d,12,2,false,0.1,0.23333"""
    val dimension1 = ScalableBuilder[Dimension]
      .setField(_.c, f => 12L)
      .build(line, ',')
      .toScala

    println(dimension1)
    assert(dimension1.toString == "Some(Dimension(abc,Some({\"a\":\"b\",\"c\":\"d\"}),d,12,2,false,0.1,0.23333))")

    val csv = CsvableBuilder[Dimension]
      .setField[Char](_.d, f => "????????")
      .setField[Option[String]](_.value, js => s"\"${js.get.replace("\"", "\"\"")}\"")
      .build(dimension1.get, ',')
      .toCsvString

    println(csv)
    assert(csv == "abc,\"{\"\"a\"\":\"\"b\"\",\"\"c\"\":\"\"d\"\"}\",????????,12,2,false,0.1,0.23333")
  }

  "CustomConverterBuilder3" should "ok when using json value" in {
    val line = """abc,"{""a"":""b"",""c"":""d""}",d,12,2,false,0.1,0.23333"""
    val d = ScalableBuilder[Dimension]
      .setField(_.value, s => None)
      .build(line, ',')
      .toScala
    assert(d.toString == "Some(Dimension(abc,None,d,12,2,false,0.1,0.23333))")

    val d2 = ScalableBuilder[Dimension]
      .setField(_.value, s => None)
      .build("""abc,"{""a"":""b"",""c"":""d""}",d,12,2,false,0.1,0.23333""", ',')
      .toScala
    assert(d2.toString == "Some(Dimension(abc,None,d,12,2,false,0.1,0.23333))")

    val e = ScalableBuilder[Dimension]
      .build(line, ',')
      .toScala
    println(e)

    assert(e.toString == "Some(Dimension(abc,Some({\"a\":\"b\",\"c\":\"d\"}),d,12,2,false,0.1,0.23333))")
  }

  "CustomConverterBuilder4" should "ok when using toCsvString" in {
    val e = Dimension("1", Some("hello"), 'c', 1L, 1, false, 0.1F, 0.2)
    val dimension1 = CsvableBuilder[Dimension]
      .build(e, ',').toCsvString
    assert(dimension1 == "1,hello,c,1,1,false,0.1,0.2")

    val dimension2 = CsvableBuilder[Dimension]
      .setField[Option[String]](_.value, _ => "hello world")
      .build(e, '*')
      .toCsvString
    assert(dimension2 == "1*hello world*c*1*1*false*0.1*0.2")

    val dimension3 = CsvableBuilder[Dimension]
      .setField[Option[String]](_.value, _ => "hello world")
      .build(Dimension("1", Some("hello"), 'c', 1L, 1, false, 0.1F, 0.2), ',')
      .toCsvString
    assert(dimension3 == "1,hello world,c,1,1,false,0.1,0.2")
  }


  "CustomConverterBuilder5" should "ok when using list" in {
    val es = List(
      Dimension("1", Some("hello"), 'c', 1L, 1, true, 0.1F, 0.2),
      Dimension("2", Some("hello bitlap"), 'c', 1L, 1, false, 0.1F, 0.2)
    )

    val dimension1 = es.map(e => CsvableBuilder[Dimension].build(e, ',').toCsvString)
    assert(dimension1 == List("1,hello,c,1,1,true,0.1,0.2", "2,hello bitlap,c,1,1,false,0.1,0.2"))
    
    val csv = List("1,hello,c,1,1,true,0.1,0.2", "2,hello bitlap,c,1,1,false,0.1,0.2")
    val scala = csv.map(f => ScalableBuilder[Dimension].build(f, ',').toScala)
    assert(scala.toString() == "List(Some(Dimension(1,Some(hello),c,1,1,true,0.1,0.2)), Some(Dimension(2,Some(hello bitlap),c,1,1,false,0.1,0.2)))")

  }
}