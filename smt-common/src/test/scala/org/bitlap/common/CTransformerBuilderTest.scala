package org.bitlap.common

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** @author
 *    梦境迷离
 *  @version 1.0,6/15/22
 */
class CTransformerBuilderTest extends AnyFlatSpec with Matchers {

  "CTransformerBuilderTest1" should "ok in simple to use" in {

    case class A(a: String, b: Int, c: Long, d: Option[String])
    case class B(a: String, b: Int, c: Long, d: Option[String])
    val a = A("1", 1, 2, None)
    val b: B = CTransformerBuilder[A, B]
      .mapName(_.b, _.c)
      .build
      .transform(a)
    b.toString shouldEqual "B(1,1,1,None)"
  }
}
