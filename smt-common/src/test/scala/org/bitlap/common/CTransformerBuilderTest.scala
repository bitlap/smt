package org.bitlap.common

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** @author
 *    梦境迷离
 *  @version 1.0,6/15/22
 */
class CTransformerBuilderTest extends AnyFlatSpec with Matchers {

  "CTransformerBuilderTest1" should "ok in simple to use" in {

    case class A1(a: String, b: Int, cc: Long, d: Option[String])
    case class A2(a: String, b: Int, c: Int, d: Option[String])

    val a = A1("hello", 1, 2, None)
    val b: A2 = CTransformerBuilder[A1, A2] // todo `fromField: Long` type Long cannot be ignored.
      .mapField[Long, Int](_.cc, _.c, (fromField: Long) => if (fromField > 0) fromField.toInt else 0)
      .build
      .transform(a)

    b.toString shouldEqual "A2(hello,1,2,None)"
//     use implicit

    implicit val transformer = CTransformerBuilder[A1, A2]
      .mapField(_.b, _.c)
      .mapField(_.a, _.a)
      .mapField[Option[String], Option[String]](_.d, _.d, (map: Option[String]) => map)
      .build

    CTransformer[A1, A2].transform(a).toString shouldEqual "A2(hello,1,1,None)"
  }

  "CTransformerBuilderTest2" should "error if field type is incompatible" in {
    """
      |
      |    case class A1(a: String, b: Int, cc: Long, d: Option[String])
      |    case class A2(a: String, b: Int, c: Int, d: Option[String])
      |    val a = A1("hello", 1, 2, None)
      |    val b: A2 = CTransformerBuilder[A1, A2]
      |      .mapField(_.cc, _.c)
      |      .build
      |      .transform(a)
      |""".stripMargin shouldNot compile
  }

  "CTransformerBuilderTest3" should "ok when nest field" in {
    case class C1(j: Int)
    case class D1(c1: C1)
    case class C2(j: Int)
    case class D2(c2: C2)

    implicit val cTransformer: CTransformer[C1, C2] = CTransformerBuilder[C1, C2].build
    implicit val dTransformer: CTransformer[D1, D2] = CTransformerBuilder[D1, D2].mapField(_.c1, _.c2).build

    val d1     = D1(C1(1))
    val d2: D2 = CTransformer[D1, D2].transform(d1)
    println(d2)
  }
}
