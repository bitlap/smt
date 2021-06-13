package io.github.liguobin

import org.scalatest.{ FlatSpec, Matchers }

/**
 *
 * @author 梦境迷离
 * @since 2021/6/13
 * @version 1.0
 */
class ToStringTest extends FlatSpec with Matchers {

  "toString1" should "contains ContainsCtorParams" in {
    @toString
    class TestClass(val i: Int = 0, var j: Int) {
      val y: Int = 0
      var z: String = "hello"
      var x: String = "world"
    }
    val s = new TestClass(1, 2).toString
    println(s)
    assert(s == "TestClass(0, hello, world)")
  }

  "toString2" should "contains ContainsCtorParams" in {
    @toString(isContainsCtorParams = true)
    class TestClass(val i: Int = 0, var j: Int) {
      val y: Int = 0
      var z: String = "hello"
      var x: String = "world"
    }
    val s = new TestClass(1, 2).toString
    println(s)
    assert(s == "TestClass(1, 2, 0, hello, world)")
  }

  //  "toString3" should "failed when toString already defined" in {
  //    @toString(isContainsCtorParams = true)
  //    class TestClass(val i: Int = 0, var j: Int) {
  //      val y: Int = 0
  //      var z: String = "hello"
  //      var x: String = "world"
  //
  //      override def toString = s"TestClass($y, $z, $x, $i, $j)"
  //    }
  //    val s = new TestClass(1, 2).toString
  //    println(s)
  //    assert(s == "TestClass(1, 2, 0, hello world macro, hello world)")
  //  }

  "toString4" should "case class" in {
    @toString(isContainsCtorParams = true)
    case class TestClass(val i: Int = 0, var j: Int)
    val s = TestClass(1, 2).toString
    println(s)
    assert(s == "TestClass(1,2)")
  }
}
