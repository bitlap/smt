/*
 * Copyright (c) 2022 org.bitlap
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
 * @since 2021/7/3
 * @version 1.0
 */
class ConstructorTest extends AnyFlatSpec with Matchers {

  "constructor1" should "failed" in {
    """    @constructor
      |    case class A2() {
      |      private val a: Int = 1
      |      var b: Int = 1
      |      def helloWorld: String = "hello world"
      |    }""".stripMargin shouldNot compile

    """    @constructor
      |    object A2 {
      |      private val a: Int = 1
      |      var b: Int = 1
      |      def helloWorld: String = "hello world"
      |    }""".stripMargin shouldNot compile

    """    @apply @toString @builder @constructor(excludeFields=Seq("c"))
      |    class A2(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L)) {
      |      private val a: Int = 1
      |      var b: Int = 1
      |      protected var c: Int = _
      |
      |      def helloWorld: String = "hello world"
      |    }
      |    A2(1, 2, None, None).c
      |    """.stripMargin shouldNot compile

    """    @apply @toString @builder @constructor(excludeFields=Seq("c"), verbose = true) //verbose should in front
      |    class A2(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L)) {
      |      private val a: Int = 1
      |      var b: Int = 1
      |      protected var c: Int = _
      |
      |      def helloWorld: String = "hello world"
      |    }
      |    """.stripMargin shouldNot compile
  }

  "constructor2" should "ok at class" in {
    """    @constructor(verbose = true)
      |    class A2(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L)) {
      |      private val a: Int = 1
      |      private var b: Int = 1
      |      protected var c: Int = _
      |
      |      def helloWorld: String = "hello world"
      |    }""".stripMargin should compile

    """    @constructor
      |    class A2(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L)) {
      |      private val a: Int = 1
      |      var b: Int = 1
      |      protected var c: Int = _
      |
      |      def helloWorld: String = "hello world"
      |    }""".stripMargin should compile

    """    @apply @builder @toString
      |    class A2(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L)) {
      |      private val a: Int = 1
      |      var b: Int = 1
      |      protected var c: Int = _
      |
      |      def helloWorld: String = "hello world"
      |    }""".stripMargin should compile

    """    @apply @builder @toString @constructor
      |    class A2(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L)) {
      |      private val a: Int = 1
      |      var b: Int = 1
      |      protected var c: Int = _
      |
      |      def helloWorld: String = "hello world"
      |    }""".stripMargin should compile

    """    @builder @apply @toString @constructor
      |    class A2(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L)) {
      |      private val a: Int = 1
      |      var b: Int = 1
      |      protected var c: Int = _
      |
      |      def helloWorld: String = "hello world"
      |    }""".stripMargin should compile

    """    @builder @toString @apply @constructor
      |    class A2(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L)) {
      |      private val a: Int = 1
      |      var b: Int = 1
      |      protected var c: Int = _
      |
      |      def helloWorld: String = "hello world"
      |    }""".stripMargin should compile

    """    @apply @toString @builder @constructor
      |    class A2(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L)) {
      |      private val a: Int = 1
      |      var b: Int = 1
      |      protected var c: Int = _
      |
      |      def helloWorld: String = "hello world"
      |    }""".stripMargin should compile

    """    @apply @toString @builder @constructor(excludeFields=Seq("c"))
      |    class A2(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L)) {
      |      private val a: Int = 1
      |      var b: Int = 1
      |      protected var c: Int = _
      |
      |      def helloWorld: String = "hello world"
      |    }""".stripMargin should compile

    """    @apply @toString @builder @constructor(verbose = true, excludeFields=Seq("c"))
      |    class A2(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L)) {
      |      private val a: Int = 1
      |      var b: Int = 1
      |      protected var c: Int = _
      |
      |      def helloWorld: String = "hello world"
      |    }""".stripMargin should compile
  }

  "constructor3" should "ok when construction is  currying" in {
    @apply
    @toString
    @builder
    @constructor(excludeFields = Seq("c"))
    class A2(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L)) {
      private val a: Int = 1
      var b: Int = 1
      protected var c: Int = _

      def helloWorld: String = "hello world"
    }

    println(A2(1, 2, None, Some(12L)))
    println(A2.builder().int(1).j(2).build())
    println(new A2(1, 2, None, None, 100))

    @toString
    @constructor
    class TestClass12(val i: Int = 0)(var j: Int)(val k: Int) {
      private val a: Int = 1
      var b: Int = 1
      protected var c: Int = _
    }

    println(new TestClass12(1, 1, 1)(2)(3))

    /**
     * def <init>(i: Int, b: Int, c: Int)(j: Int)(k: Int) = {
     * <init>(i)(j)(k);
     * this.b = b;
     * this.c = c
     * }
     */

  }

  "constructor4" should "ok when type is omitted" in {
    @toString
    class B
    @toString
    @constructor
    class TestClass12(val i: Int = 0)(var j: Int)(val k: Int) {
      var b = "hello" //primitive type, support no type declared
      var c: B = new B() //not support no type declared, `var c = new B ()` cannot be compiled.
    }
    val t = new TestClass12(1, "helloo", new B())(1)(1)
    println(t)

  }

  "constructor5" should "ok when type is generic" in {
    @toString
    @constructor
    class TestClass12[T, U](val i: U)(var j: Int)(val k: T) {
      var b: List[T] = _
    }
    val t = new TestClass12(1, List("helloo"))(1)("1")
    println(t)
  }

}
