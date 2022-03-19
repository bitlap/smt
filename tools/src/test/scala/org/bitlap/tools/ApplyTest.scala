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
 * @since 2021/6/30
 * @version 1.0
 */
class ApplyTest extends AnyFlatSpec with Matchers {

  "apply1" should "ok at class" in {
    // int: Int => private[this] val int: Int = _;
    // val j: Int => val j: Int = _;
    // apply => def apply(int: Int, j: Int, k: Option[String] = None, t: Option[Long] = Some(1L)): A = new A(int, j, k, t)
    """@apply(verbose = true) class C2(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L))(o: Int = 1)""" should compile
    """@toString @apply class A(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L))""" should compile
    @toString
    @apply class A2(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L))
    println(A2(1, 2, None, None))

    """@apply @toString class B(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L))""" should compile
    @apply
    @toString class B2(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L))
    println(B2(1, 2, None, None))

    // exists object
    """@apply @toString class B(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L));object B3""" should compile
    @apply
    @toString class B3(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L))
    object B3
    println(B3(1, 2, None, None))
  }
  "apply2" should "failed on case class" in {
    """@apply @toString case class C3(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L))(o: Int = 1)""" shouldNot compile
  }

  "apply3" should "ok with currying" in {
    """@apply @toString class C2(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L))(o: Int = 1)""" should compile
    @apply
    @toString class C1(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L))(o: Int = 1)
    @apply
    @toString class B3(int: Int)(val j: Int)(var k: Option[String] = None)(t: Option[Long] = Some(1L))
    @apply
    @toString class B4(int: Int, a: Seq[Seq[String]])(val j: Int, b: Seq[String])(var k: Option[String] = None, c: Seq[Option[String]])(t: Option[Long] = Some(1L))
  }

  "apply4" should "ok with generic" in {
    @apply
    @toString class B3[T, U](int: T, yy: Int)(val j: U)
    println(B3(1, 2)(2))

    @toString
    @apply class B4[T, U](int: T, val j: U)
    println(B4("helloworld", 2))

    @toString
    @apply class B5[T <: Any, U](int: T, val j: U)
    println(B5("helloworld", 2))
  }

  "apply5" should "failed when input not invalid" in {
    """@apply(true) class C2(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L))(o: Int = 1)""" shouldNot compile
  }

  "apply6" should "ok with Enumeration" in {
    object TestEnum extends Enumeration {
      type TestEnum = Value
      val E1 = Value(1, "E1")
      val E2 = Value(2, "E2")
    }
    @apply class TestEnumA1(e: TestEnum.TestEnum)
    println(TestEnumA1(TestEnum.E1))
  }
}
