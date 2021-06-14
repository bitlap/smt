package io.github.liguobin

import org.scalatest.{FlatSpec, Matchers}

/**
 *
 * @author 梦境迷离
 * @since 2021/6/13
 * @version 1.0
 */
class ToStringTest extends FlatSpec with Matchers {

  "toString1" should "not contains constructors parameters" in {
    @toString(false, false, false)
    class TestClass(val i: Int = 0, var j: Int) {
      val y: Int = 0
      var z: String = "hello"
      var x: String = "world"
    }
    val s = new TestClass(1, 2).toString
    println(s)
    assert(s == "TestClass(0, hello, world)")
  }

  "toString2" should "contains constructors parameters" in {
    @toString(true, true, true)
    class TestClass(val i: Int = 0, var j: Int) {
      val y: Int = 0
      var z: String = "hello"
      var x: String = "world"
    }
    val s = new TestClass(1, 2).toString
    println(s)
    assert(s == "TestClass(i=1, j=2, y=0, z=hello, x=world)")
  }

  "toString3" should "not contains constructors parameters but with name" in {
    @toString(true, false, true)
    class TestClass(val i: Int = 0, var j: Int) {
      val y: Int = 0
      var z: String = "hello"
      var x: String = "world"
    }
    val s = new TestClass(1, 2).toString
    println(s)
    assert(s == "TestClass(y=0, z=hello, x=world)")
  }

  "toString4" should "contains constructors parameters but without name" in {
    @toString(true, true, false)
    class TestClass(val i: Int = 0, var j: Int) {
      val y: Int = 0
      var z: String = "hello"
      var x: String = "world"
    }
    val s = new TestClass(1, 2).toString
    println(s)
    assert(s == "TestClass(1, 2, 0, hello, world)")
  }

  "toString5" should "case class without name" in {
    @toString(true, false, false)
    case class TestClass(i: Int = 0, var j: Int) {
      val y: Int = 0
      var z: String = "hello"
      var x: String = "world"
    }
    val s = TestClass(1, 2).toString
    println(s)
    assert(s == "TestClass(0, hello, world)")
  }

  "toString6" should "case class with name" in {
    @toString(true, false, true)
    case class TestClass(i: Int = 0, var j: Int) {
      val y: Int = 0
      var z: String = "hello"
      var x: String = "world"
    }
    case class TestClass2(i: Int = 0, var j: Int) // No method body, use default toString
    val s = TestClass(1, 2).toString
    val s2 = TestClass2(1, 2).toString
    println(s)
    println(s2)

    assert(s == "TestClass(y=0, z=hello, x=world)")
    assert(s2 == "TestClass2(1,2)")
  }

  "toString7" should "case class with name" in {
    @toString(true, true, true)
    case class TestClass(i: Int = 0, var j: Int) {
      val y: Int = 0
      var z: String = "hello"
      var x: String = "world"
    }
    val s = TestClass(1, 2).toString
    println(s)
    assert(s == "TestClass(i=1, j=2, y=0, z=hello, x=world)")
  }

  "toString8" should "case class with name and itself" in {
    @toString(true, true, true)
    case class TestClass(i: Int = 0, var j: Int, k: TestClass) {
      val y: Int = 0
      var z: String = "hello"
      var x: String = "world"
    }
    val s = TestClass(1, 2, TestClass(1, 2, null)).toString
    println(s)
    assert(s == "TestClass(i=1, j=2, k=TestClass(i=1, j=2, k=null, y=0, z=hello, x=world), y=0, z=hello, x=world)")
  }

  "toString9" should "case class with name and itself2" in {
    @toString(true, true, true)
    case class TestClass(i: Int = 0, var j: Int) {
      val y: Int = 0
      var z: String = "hello"
      var x: String = "world"
      val t: TestClass = null // if not null, will error
    }
    val s = TestClass(1, 2).toString
    println(s)
    assert(s == "TestClass(i=1, j=2, y=0, z=hello, x=world, t=null)")
  }

  "toString10" should "case class with name and itself3" in {
    @toString(true, true, true)
    case class TestClass(i: Int = 0, var j: Int, k: TestClass) {
      val y: Int = 0
      var z: String = "hello"
      var x: String = "world"
    }
    val s = TestClass(1, 2, TestClass(1, 2, TestClass(1, 3, null))).toString
    println(s)
    assert(s == "TestClass(i=1, j=2, k=TestClass(i=1, j=2, k=TestClass(i=1, j=3, k=null, y=0, z=hello, x=world), y=0, z=hello, x=world), y=0, z=hello, x=world)")
  }

  "toString11" should "class with name and code block contains method" in {
    @toString(true, true, true)
    class TestClass(i: Int = 0, var j: Int) {
      def helloWorld: String = i + ""

      println(helloWorld)

      //      override def toString = s"TestClass(j=$j, i=$i)" // scalac override def toString = StringContext("TestClass(j=", ", i=", ")").s(j, i)
    }
    val s = new TestClass(1, 2).toString
    println(s)
    assert(s == "TestClass(i=1, j=2)")
  }

  "toString12" should "class with name and not code block" in {
    @toString(true, false, true)
    class TestClass(i: Int = 0, var j: Int)
    val s = new TestClass(1, 2).toString
    println(s)
    assert(s == "TestClass()")

    @toString(true, true, true)
    class TestClass2(i: Int = 1, var j: Int = 2)
    val s2 = new TestClass2(1, 2).toString
    println(s2)
    assert(s2 == "TestClass2(i=1, j=2)")

    @toString(true, true, false)
    class TestClass3(i: Int = 1, var j: Int = 3)
    val s3 = new TestClass3(1, 2).toString
    println(s3)
    assert(s3 == "TestClass3(1, 2)")
  }

  "toString13" should "case class with name and not code block" in {
    @toString(true, true, false)
    case class TestClass(i: Int = 1, var j: Int = 3)
    val s = TestClass(1, 2).toString
    println(s)
    assert(s == "TestClass(1, 2)")

    @toString(true, false, false)
    case class TestClass2(i: Int = 1, var j: Int = 3)
    val s2 = TestClass2(1, 2).toString
    println(s2)
    assert(s2 == "TestClass2()")

    @toString(true, true, true)
    case class TestClass3(i: Int = 1, var j: Int = 3)
    val s3 = TestClass3(1, 2).toString
    println(s3)
    assert(s3 == "TestClass3(i=1, j=2)")
  }
}
