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

package org.bitlap.tools

import play.api.libs.json.Json
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** @author
 *    梦境迷离
 *  @since 2021/6/18
 *  @version 1.0
 */
class JsonTest extends AnyFlatSpec with Matchers {

  // class must be wrote here

  @json
  case class TestClass1(val i: Int = 0, var j: Int, x: String, o: Option[String] = Some(""))

  object TestClass1

  @json
  case class TestClass2(val i: Int = 0, var j: Int, x: String, o: Option[String] = Some(""))

  @json
  @SerialVersionUID(1L)
  case class TestClass3(val i: Int = 0, var j: Int, x: String, o: Option[String] = Some(""))

  "json1" should "println case class, exists companion object" in {
    val ret = Json.prettyPrint(Json.toJson(TestClass1(1, 2, "")))
    println(ret)
    assert(ret == "{\n  \"i\" : 1,\n  \"j\" : 2,\n  \"x\" : \"\",\n  \"o\" : \"\"\n}")
  }

  "json2" should "println case class, no companion object" in {
    val json = Json.toJson(TestClass2(1, 2, ""))
    val ret  = Json.prettyPrint(json)
    println(Json.fromJson[TestClass2](json))
    println(ret)
    assert(ret == "{\n  \"i\" : 1,\n  \"j\" : 2,\n  \"x\" : \"\",\n  \"o\" : \"\"\n}")
  }

  "json3" should "println case class, contains other annotation" in {
    val ret = Json.prettyPrint(Json.toJson(TestClass3(1, 2, "")))
    println(ret)
    assert(ret == "{\n  \"i\" : 1,\n  \"j\" : 2,\n  \"x\" : \"\",\n  \"o\" : \"\"\n}")
  }

  "json4" should "failed on normal class" in {
    """
      |  @json
      |  @SerialVersionUID(1L)
      |  class TestClass3(val i: Int = 0, var j: Int, x: String, o: Option[String] = Some(""))
      |""".stripMargin shouldNot compile
  }
}
