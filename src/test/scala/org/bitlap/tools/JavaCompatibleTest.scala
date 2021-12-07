/*
 * Copyright (c) 2021 org.bitlap
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
 *
 * @author 梦境迷离
 * @since 2021/11/23
 * @version 1.0
 */
class JavaCompatibleTest extends AnyFlatSpec with Matchers {

  "JavaCompatible1" should "ok" in {
    """
      |    @JavaCompatible
      |    case class A(a: Int, b: Short, c: Byte, d: Double, e: Float, f: Long, g: Char, h: Boolean)
      |    val t = new A()
      |    assert(t.a == 0 && t.g == '?')
      |""".stripMargin should compile
  }

  "JavaCompatible2" should "ok" in {
    """
      |    @JavaCompatible
      |    case class A(a: Int, b: Short, c: Byte, d: Double)(val e: Float, val f: Long)(val g: Char, val h: Boolean)
      |    val t = new A()
      |    assert(t.a == 0 && t.g == '?')
      |""".stripMargin should compile
  }

  "JavaCompatible3" should "failed" in {
    """
      |    @JavaCompatible
      |    case class A(a: Int, b: Short, c: Byte, d: Double)(val e: Float, val f: Long)(g: Char, h: Boolean)
      |    val t = new A()
      |    assert(t.a == 0 && t.g == '?')
      |""".stripMargin shouldNot compile

    """
      |    @JavaCompatible
      |    class A(val a: Int, val b: Short)
      |    val t = new A()
      |    assert(t.a == 0)
      |""".stripMargin shouldNot compile
  }

  "JavaCompatible4" should "ok" in {
    @JavaCompatible
    case class A(a: Int, b: Short, c: Byte, d: Double, e: Float, f: Long, g: Char, h: Boolean, i: String)
    val t = new A()
    assert(t.a == 0 && t.g == '?')
  }

  "JavaCompatible5" should "ok" in {
    import scala.beans.BeanProperty
    @JavaCompatible
    case class A(@BeanProperty a: Int, b: Short, c: Byte, d: Double, e: Float, f: Long, g: Char, h: Boolean, i: String)
    val t = new A()
    assert(t.a == 0 && t.g == '?')
  }

  "JavaCompatible6" should "ok when exists @BeanProperty" in {
    import scala.beans.BeanProperty
    @JavaCompatible
    case class A(@BeanProperty a: Int, b: Short, c: Byte, d: Double, e: Float, f: Long, g: Char, h: Boolean, i: String)
    val t = new A()
    assert(t.getA == 0)
    assert(t.getB == 0)
  }

  "JavaCompatible7" should "ok when exists super" in {
    import scala.beans.BeanProperty
    class B(@BeanProperty val name: String, @BeanProperty val id: Int)
    @JavaCompatible
    case class A(a: Int, b: Short, override val name: String, override val id: Int) extends B(name, id)
    val t = new A()
    assert(t.getA == 0)
    assert(t.getB == 0)
  }

  // Why this code compile failed but test in """ """.stripMargin will pass?
  "JavaCompatible8" should "fail when exists super but not use @BeanProperty" in {
    """
      |    class B(val name: String, val id: Int)
      |    @JavaCompatible
      |    case class A(a: Int, b: Short, override val name: String, override val id: Int) extends B(name, id)
      |    val t = new A()
      |""".stripMargin should compile
  }
}
