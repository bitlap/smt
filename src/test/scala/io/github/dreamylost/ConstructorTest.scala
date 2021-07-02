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

  }

}
