//package io.github.dreamylost
//
//import org.scalatest.{ FlatSpec, Matchers }
//
///**
// *
// * @author 梦境迷离
// * @since 2021/6/30
// * @version 1.0
// */
//class ApplyTest extends FlatSpec with Matchers {
//
//  "apply1" should "ok at class" in {
//    // int: Int => private[this] val int: Int = _;
//    // val j: Int => val j: Int = _;
//    // apply => def apply(int: Int, j: Int, k: Option[String] = None, t: Option[Long] = Some(1L)): A = new A(int, j, k, t)
//
//    """@toString @apply class A(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L))""" should compile
//    @toString
//    @apply class A2(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L))
//    println(A2(1, 2))
//
//    """@apply @toString class B(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L))""" should compile
//    @apply
//    @toString class B2(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L))
//    println(B2(1, 2))
//  }
//
//  "apply2" should "failed at class" in {
//    // FAILED, not support currying!!
//    """@apply @toString class C(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L))(o: Int = 1)""" shouldNot compile
//  }
//}
