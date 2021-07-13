package io.github.dreamylost

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
  "apply2" should "failed at class" in {
    // FAILED, not support currying!!
    """@apply @toString class C(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L))(o: Int = 1)""" shouldNot compile
  }

  "apply3" should "ok with currying" in {
    @apply
    @toString class B3(int: Int)(val j: Int)(var k: Option[String] = None)(t: Option[Long] = Some(1L))
  }

  "apply4" should "ok with generic" in {
    @apply
    @toString class B3[T, U](int: T)(val j: U)
    println(B3(1)(2))

    @toString
    @apply class B4[T, U](int: T, val j: U)
    println(B4("helloworld", 2))

  }
}
