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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * @author 梦境迷离
 * @since 2021/6/19
 * @version 1.0
 */
class BuilderTest extends AnyFlatSpec with Matchers {

  "builder1" should "case class, non companion object" in {
    @builder
    case class TestClass1(val i: Int = 0, var j: Int, x: String, o: Option[String] = Some(""))
    // field : <caseaccessor> <paramaccessor> val i: Int = 0， so default value is "_"
    val ret = TestClass1.builder().i(1).j(0).x("x").build()
    println(ret)
    assert(TestClass1.builder().getClass.getTypeName == "org.bitlap.tools.BuilderTest$TestClass1$2$TestClass1Builder")
    assert(ret.toString == "TestClass1(1,0,x,Some())")

  }
  "builder2" should "case class with companion object" in {
    @builder
    case class TestClass1(val i: Int = 0, var j: Int, x: String, o: Option[String] = Some(""))
    object TestClass1
    val ret = TestClass1.builder().i(1).j(0).x("x").build()
    println(ret)
    assert(ret.toString == "TestClass1(1,0,x,Some())")
  }

  "builder3" should "class with toString, non companion object" in {
    @toString //"toString" must be before "builder"
    @builder
    class TestClass1(val i: Int = 0, var j: Int, x: String, o: Option[String] = Some(""))
    val ret = TestClass1.builder().i(1).j(0).x("x").build()
    println(ret)
    assert(ret.toString == "TestClass1(i=1, j=0, x=x, o=Some())")
  }

  "builder4" should "class toString and companion object" in {
    @toString
    @builder
    class TestClass1(val i: Int = 0, var j: Int, x: String, o: Option[String] = Some(""))
    object TestClass1
    val ret = TestClass1.builder().i(1).j(0).x("x").build()
    assert(ret.toString == "TestClass1(i=1, j=0, x=x, o=Some())")
  }

  "builder5" should "case class with toString and companion object" in {
    @toString
    @builder
    case class TestClass1(val i: Int = 0, var j: Int, x: String, o: Option[String] = Some(""))
    object TestClass1
    val ret = TestClass1.builder().i(1).j(0).x("x").build()
    println(ret)
    assert(ret.toString == "TestClass1(i=1, j=0, x=x, o=Some())")
  }

  "builder6" should "case class with toString, non companion object" in {
    @toString
    @builder
    case class TestClass1(val i: Int = 0, var j: Int, x: String, o: Option[String] = Some(""))
    val ret = TestClass1.builder().i(1).j(0).x("x").build()
    println(ret)
    assert(ret.toString == "TestClass1(i=1, j=0, x=x, o=Some())")
  }

  "builder7" should "case class with toString and companion object not in order" in {
    @builder
    @toString //failed when companion object exists, fix in 0.0.6
    case class TestClass1(val i: Int = 0, var j: Int, x: String, o: Option[String] = Some(""))
    object TestClass1
    val ret = TestClass1.builder().i(1).j(0).x("x").build()
    println(ret)
    assert(ret.toString == "TestClass1(i=1, j=0, x=x, o=Some())")
  }

  "builder8" should "ok on currying" in {

    @builder
    case class TestClass11(val i: Int = 0)(var j: Int)(val k: Int)(val t: Option[String])

    @builder
    class TestClass12(val i: Int = 0)(var j: Int)(val k: Int)(val t: Option[String])
  }

  "builder9" should "ok with generic" in {

    @builder
    case class TestClass11[T](i: T)(var j: Int)(val k: Int)(val t: Option[String])

    val a = TestClass11.builder().i("hello generic").j(1).k(22).t(None).build()
    println(a)

    @builder
    case class TestClass12[T, U](val i: T, var j: U)
    val b = TestClass12.builder().i(new AnyRef).j(List("helloworld", "generic is ok")).build()
    println(b)

    @builder
    case class TestClass13[T <: Any, U](val i: T, var j: U)
    val c = TestClass13.builder().i(1).j(List("helloworld", "generic is ok")).build()
    println(c)
  }
}
