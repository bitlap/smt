/*
 * Copyright (c) 2021 jxnu-liguobin && contributors
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

package io.github.dreamylost

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 *
 * @author 梦境迷离
 * @since 2021/7/18
 * @version 1.0
 */
class EqualsAndHashCodeTest extends AnyFlatSpec with Matchers {

  @equalsAndHashCode(verbose = true)
  @toString
  class Employee(name: String, age: Int, var role: String) extends Person(name, age)

  @toString
  @equalsAndHashCode(verbose = true)
  class Person(var name: String, var age: Int)

  "equals1" should "ok" in {
    // these first two instances should be equal
    val nimoy = new Person("Leonard Nimoy", 82)
    val nimoy2 = new Person("Leonard Nimoy", 82)
    val shatner = new Person("William Shatner", 82)
    val stewart = new Person("Patrick Stewart", 47)

    println(nimoy)
    println(nimoy.hashCode())

    // all tests pass
    assert(nimoy != null)

    // canEqual
    assert(nimoy.canEqual(nimoy))
    assert(nimoy.canEqual(nimoy2))
    assert(nimoy2.canEqual(nimoy))

    // these should be equal
    assert(nimoy == nimoy)
    assert(nimoy == nimoy2)
    assert(nimoy2 == nimoy)

    // these should not be equal
    assert(nimoy != shatner)
    assert(shatner != nimoy)
    assert(nimoy != "Leonard Nimoy")
    assert(nimoy != stewart)
  }

  "equals2" should "ok" in {
    // these first two instance should be equal
    val eNimoy1 = new Employee("Leonard Nimoy", 82, "Actor")
    val eNimoy2 = new Employee("Leonard Nimoy", 82, "Actor")
    val pNimoy = new Person("Leonard Nimoy", 82)
    val eShatner = new Employee("William Shatner", 82, "Actor")

    // canEqual
    assert(eNimoy1.canEqual(eNimoy1))
    assert(eNimoy1.canEqual(eNimoy2))
    assert(eNimoy2.canEqual(eNimoy1))

    // equality tests
    assert(eNimoy1 == eNimoy1)
    assert(eNimoy1 == eNimoy2)
    assert(eNimoy2 == eNimoy1)

    // non-equality tests
    assert(eNimoy1 != pNimoy)
    assert(pNimoy != eNimoy1)
    assert(eNimoy1 != eShatner)
    assert(eShatner != eNimoy1)

    println(eNimoy1)
    println(eNimoy1.hashCode())
  }

  "equals3" should "ok even if exists a canEqual" in {
    @equalsAndHashCode
    class Employee1(name: String, age: Int, var role: String) extends Person(name, age) {
      class A {}
      override def canEqual(that: Any) = that.getClass == classOf[Employee1]
    }
    """
      |    @equalsAndHashCode
      |    class Employee2(name: String, age: Int, var role: String) extends Person(name, age) {
      |      override def canEqual(that: Any) = that.getClass == classOf[Employee];
      |    }
      |""".stripMargin should compile
  }

  "equals4" should "ok when there are members" in {
    @equalsAndHashCode
    class Employee1(name: String, age: Int, var role: String) extends Person(name, age) {
      val i = 0
    }
    """
      |    @equalsAndHashCode
      |    class Employee2(name: String, age: Int, var role: String) extends Person(name, age) {
      |      val i = 0
      |    }
      |""".stripMargin should compile

    @equalsAndHashCode
    class Employee3(name: String, age: Int, var role: String) extends Person(name, age) {
      val i = 0

      def hello: String = ???

      class A {}
    }
    """
      |    @equalsAndHashCode(excludeFields = Nil)
      |    class Employee4(name: String, age: Int, var role: String) extends Person(name, age) {
      |      val i = 0
      |      def hello: String = ???
      |    }
      |""".stripMargin should compile

    """
      |    @equalsAndHashCode(excludeFields = Seq("avatar3", "sex2", "avatar2"))
      |    class Employee5(name: String, age: Int, var role: String, private [this] val sex1: Int, protected [this] val avatar1: String) extends Person(name, age) {
      |      protected [this] var sex2: Int = _
      |      private [this] val avatar2: String = ""
      |      private [this] val avatar3 = "http"
      |      protected [this] val avatar4 = "http"
      |
      |      val i = 0
      |      def hello: String = ???
      |    }
      |""".stripMargin should compile
  }

  "equals5" should "failed when input not in order" in {
    """
      |    @equalsAndHashCode(excludeFields = Nil, verbose = true)
      |    class Employee4(name: String, age: Int, var role: String) extends Person(name, age) {
      |      val i = 0
      |      def hello: String = ???
      |    }
      |""".stripMargin shouldNot compile

    """
      |    @equalsAndHashCode(verbose = true, excludeFields = Seq("i", "j", "k", "t"))
      |    class Employee5(name: String, age: Int, var role: String) extends Person(name, age) {
      |      val i = 0
      |      var j = 0
      |      val k: Int = 1
      |      var t: Int = 2
      |      def hello: String = ???
      |    }
      |""".stripMargin should compile

    """
      |    @equalsAndHashCode(verbose = true, excludeFields = Seq("i", "j"))
      |    class Employee6(name: String, age: Int, var role: String) extends Person(name, age) {
      |      val i = 0
      |      var j = 0
      |      val k: Int = 1
      |      var t: Int = 2
      |      def hello: String = ???
      |    }
      |""".stripMargin should compile
  }

  "equals6" should "failed when equals method has private[this] or protected[this]" in {
    """
      | class A(private[this] val k: Int = 0, protected[this] val t: Int = 0) {
      |    val i = 0
      |    private val j = 0
      |    protected[this] val k2: Int = 0
      |    protected[this] val t2: Int = 0
      |
      |    def canEqual(other: Any): Boolean = other.isInstanceOf[A]
      |
      |    override def equals(other: Any): Boolean = other match {
      |      case that: A =>
      |        (that canEqual this) &&
      |          i == that.i &&
      |          j == that.j &&
      |          k2 == that.k2 &&
      |          t2 == that.t2 &&
      |          k == that.k &&
      |          t == that.t
      |      case _ => false
      |    }
      |
      |    override def hashCode(): Int = {
      |      val state = Seq(i, j, k2, t2, k, t)
      |      state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
      |    }
      |  }
      |""".stripMargin shouldNot compile
  }

  "equals7" should "failed on case class" in {
    """
      |    @equalsAndHashCode(excludeFields = Nil)
      |    case class Employee4(name: String, age: Int, var role: String) extends Person(name, age) {
      |      val i = 0
      |      def hello: String = ???
      |    }
      |""".stripMargin shouldNot compile
  }
}
