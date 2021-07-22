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
 * @author li.guobin@immomo.com
 * @version 1.0,2021/7/22
 */
class DiMacroTest extends AnyFlatSpec with Matchers {

  "wire1" should "ok" in {
    import io.github.dreamylost.di.DiMacro._
    class A()
    class B()
    class C(a: A, b: B) {
      override def toString = s"${super.toString}($a, $b)"
    }
    class D(b: B, c: C) {
      override def toString = s"${super.toString}($b, $c)"
    }
    trait Service {

      val a = AutoWire[A]
      val bbb = AutoWire[B]
      val c = AutoWire[C]
      val d = AutoWire[D]

      println(a)
      println(bbb)
      println(c)
      println(d)
    }
  }

  "wire2" should "ok by parameters inherit" in {
    import io.github.dreamylost.di.DiMacro._

    class A()
    class B() extends A
    class B1
    class C(b1: B1, a: A) {
      override def toString = s"${super.toString}($b1, $a)"
    }
    trait Service {

      //The instance required by the constructor of the class injected by autowire must be visible within the scope of the enclosingClass.
      val b1 = AutoWire[B1]
      val b = AutoWire[B]
      val c = AutoWire[C]
      println(b1)
      println(b)
      println(c)

    }
  }

  "wire3" should "ok by lazy and def" in {
    import io.github.dreamylost.di.DiMacro._

    class A()
    class B() extends A
    class B1
    class C(b1: B1, a: A) {
      override def toString = s"${super.toString}($b1, $a)"
    }
    trait Service {

      lazy val b1 = AutoWire[B1]

      def b: B = AutoWire[B]

      val c = AutoWire[C]
      println(b1)
      println(b)
      println(c)

    }
  }

}
