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

import org.bitlap.csv.core.{ CsvableBuilder, ScalableBuilder }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.bitlap.csv.core.DefaultCsvFormat

/** @author
 *    梦境迷离
 *  @version 1.0,2022/4/29
 */
class CustomConverterBuilderTest extends AnyFlatSpec with Matchers {

  "CustomConverterBuilder1" should "ok" in {
    val line      = "abc,cdf,d,12,2,false,0.1,0.23333"
    val dimension = ScalableBuilder[Dimension2].convert(line)
    assert(dimension.toString == "Some(Dimension2(abc,Some(cdf),d,12,2,false,0.1,0.23333))")
    val csv = CsvableBuilder[Dimension2].convert(dimension.get)
    println(csv)
    assert(csv == line)
  }

  "CustomConverterBuilder2" should "ok when using json value" in {
    val line = """abc,"{""a"":""b"",""c"":""d""}",d,12,2,false,0.1,0.23333"""
    val dimension1 = ScalableBuilder[Dimension2]
      .setField(_.c, _ => 12L)
      .convert(line)

    println(dimension1)
    assert(dimension1.toString == "Some(Dimension2(abc,Some({\"a\":\"b\",\"c\":\"d\"}),d,12,2,false,0.1,0.23333))")

    val csv = CsvableBuilder[Dimension2]
      .setField[Char](_.d, _ => "????????")
      .setField[Option[String]](_.value, js => s"""\"${js.get.replace("\"", "\"\"")}\"""")
      .convert(dimension1.get)

    println(csv)
    assert(csv == "abc,\"{\"\"a\"\":\"\"b\"\",\"\"c\"\":\"\"d\"\"}\",????????,12,2,false,0.1,0.23333")
  }

  "CustomConverterBuilder3" should "ok when using json value" in {
    val line = """abc,"{""a"":""b"",""c"":""d""}",d,12,2,false,0.1,0.23333"""
    val d = ScalableBuilder[Dimension2]
      .setField(_.value, _ => None)
      .convert(line)
    assert(d.toString == "Some(Dimension2(abc,None,d,12,2,false,0.1,0.23333))")

    val d2 = ScalableBuilder[Dimension2]
      .setField(_.value, _ => None)
      .convert("""abc,"{""a"":""b"",""c"":""d""}",d,12,2,false,0.1,0.23333""")
    assert(d2.toString == "Some(Dimension2(abc,None,d,12,2,false,0.1,0.23333))")

    val e = ScalableBuilder[Dimension2]
      .convert(line)
    println(e)

    assert(e.toString == "Some(Dimension2(abc,Some({\"a\":\"b\",\"c\":\"d\"}),d,12,2,false,0.1,0.23333))")
  }

  "CustomConverterBuilder4" should "ok when using toCsvString" in {
    val e = Dimension2("1", Some("hello"), 'c', 1L, 1, false, 0.1f, 0.2)
    val dimension1 = CsvableBuilder[Dimension2]
      .convert(e)
    assert(dimension1 == "1,hello,c,1,1,false,0.1,0.2")

    val dimension2 = CsvableBuilder[Dimension2]
      .setField[Option[String]](_.value, _ => "hello world")
      .convert(e)(new DefaultCsvFormat {
        override val delimiter: Char = '*'
      })
    assert(dimension2 == "1*hello world*c*1*1*false*0.1*0.2")

    val dimension3 = CsvableBuilder[Dimension2]
      .setField[Option[String]](_.value, _ => "hello world")
      .convert(Dimension2("1", Some("hello"), 'c', 1L, 1, false, 0.1f, 0.2))
    assert(dimension3 == "1,hello world,c,1,1,false,0.1,0.2")
  }

  "CustomConverterBuilder5" should "ok when using list" in {
    val es = List(
      Dimension2("1", Some("hello"), 'c', 1L, 1, true, 0.1f, 0.2),
      Dimension2("2", Some("hello bitlap"), 'c', 1L, 1, false, 0.1f, 0.2)
    )

    val dimension1 = es.map(e => CsvableBuilder[Dimension2].convert(e))
    assert(dimension1 == List("1,hello,c,1,1,true,0.1,0.2", "2,hello bitlap,c,1,1,false,0.1,0.2"))

    val csv   = List("1,hello,c,1,1,true,0.1,0.2", "2,hello bitlap,c,1,1,false,0.1,0.2")
    val scala = csv.map(f => ScalableBuilder[Dimension2].convert(f))
    assert(
      scala.toString() == "List(Some(Dimension2(1,Some(hello),c,1,1,true,0.1,0.2)), Some(Dimension2(2,Some(hello bitlap),c,1,1,false,0.1,0.2)))"
    )

  }

  "CustomConverterBuilder6" should "fail when find List or Seq but without using setFiled" in {
    """
      |ScalableBuilder[Metric2].convert(csv)
      |""".stripMargin shouldNot compile

    """
      |CsvableBuilder[Metric2].convert(metric)
      |""".stripMargin shouldNot compile

  }

  "CustomConverterBuilder7" should "fail when find List or Seq but without using setFiled" in {
    """
      |ScalableBuilder[Metric2].convert(csv)
      |""".stripMargin shouldNot compile

    """
      |CsvableBuilder[Metric2].convert(metric2)
      |""".stripMargin shouldNot compile
  }

  "CustomConverterBuilder8" should "ok when not pass columnSeparator" in {
    val e   = Dimension2("1", Some("hello"), 'c', 1L, 1, false, 0.1f, 0.2)
    val csv = CsvableBuilder[Dimension2].convert(e)
    println(csv)
    assert(csv == "1,hello,c,1,1,false,0.1,0.2")

    val scala = ScalableBuilder[Dimension2].convert(csv)
    println(scala)
    assert(scala.get == e)
  }

  "CustomConverterBuilder9" should "fail if case class has currying" in {
    """
      |case class Test(i:Int)(j:String)
      |    val t = Test(1)("hello")
      |    CsvableBuilder[Test].convert(t)
      |""".stripMargin shouldNot compile
  }

  "CustomConverterBuilder10" should "get None when error" in {
    val e = Dimension2("1", Some("hello"), 'c', 1L, 1, false, 0.1f, 0.0)
    // aaa should be Double, but failure, can get 0.0D
    val csv   = "1,hello,c,1,1,false,0.1,aaa"
    val scala = ScalableBuilder[Dimension2].convert(csv)
    println(scala)
    assert(scala.get == e)

    val scala2 = ScalableBuilder[Dimension2].setField(_.h, _ => throw new Exception).convert(csv)
    assert(scala2.get == e)

    val scala3 = ScalableBuilder[Dimension2].setField(_.value, _ => throw new Exception).convert(csv)

    assert(scala3.get == Dimension2("1", None, 'c', 1L, 1, false, 0.1f, 0.0))
  }
}
