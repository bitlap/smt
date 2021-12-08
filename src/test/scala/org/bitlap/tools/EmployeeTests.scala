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

class EmployeeTests extends AnyFlatSpec with Matchers {

  class Employee(name: String, age: Int, var role: String)
    extends Person(name, age) {
    override def canEqual(a: Any): Boolean = a.isInstanceOf[Employee]

    override def equals(that: Any): Boolean =
      that match {
        case that: Employee =>
          that.canEqual(this) &&
            this.role == that.role &&
            super.equals(that)
        case _ => false
      }

    override def hashCode: Int = {
      val state = Seq(role)
      state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b) + super.hashCode
    }

  }

  class Person(var name: String, var age: Int) {

    // Step 1 - proper signature for `canEqual`
    // Step 2 - compare `a` to the current class
    def canEqual(a: Any): Boolean = a.isInstanceOf[Person]

    // Step 3 - proper signature for `equals`
    // Steps 4 thru 7 - implement a `match` expression
    override def equals(that: Any): Boolean =
      that match {
        case that: Person =>
          that.canEqual(this) &&
            this.name == that.name &&
            this.age == that.age
        case _ => false
      }

    // Step 8 - implement a corresponding hashCode c=method
    override def hashCode: Int = {
      val state = Seq(name, age)
      state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
    }

  }

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
