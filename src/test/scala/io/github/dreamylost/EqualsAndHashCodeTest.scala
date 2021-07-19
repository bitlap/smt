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
  class Employee(name: String, age: Int, var role: String) extends Person(name, age)

  @equalsAndHashCode(verbose = true)
  class Person(var name: String, var age: Int)

  "equals1" should "ok" in {
    // these first two instances should be equal
    val nimoy = new Person("Leonard Nimoy", 82)
    val nimoy2 = new Person("Leonard Nimoy", 82)
    val shatner = new Person("William Shatner", 82)
    val stewart = new Person("Patrick Stewart", 47)

    // all tests pass
    assert(nimoy != null)

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

    // equality tests
    assert(eNimoy1 == eNimoy1)
    assert(eNimoy1 == eNimoy2)
    assert(eNimoy2 == eNimoy1)

    // non-equality tests
    assert(eNimoy1 != pNimoy)
    assert(pNimoy != eNimoy1)
    assert(eNimoy1 != eShatner)
    assert(eShatner != eNimoy1)
  }
}
