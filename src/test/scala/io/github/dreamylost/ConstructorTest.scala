package io.github.dreamylost

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 *
 * @author 梦境迷离
 * @since 2021/7/3
 * @version 1.0
 */
class ConstructorTest extends AnyFlatSpec with Matchers {

  "constructor1" should "failed at object" in {
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
  }

  "constructor3" should "failed at object" in {
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
}
