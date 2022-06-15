package org.bitlap.common

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** @author
 *    梦境迷离
 *  @version 1.0,6/15/22
 */
class CTransformerBuilderTest extends AnyFlatSpec with Matchers {

  "CTransformerBuilderTest1" should "ok in simple to use" in {

    case class A(a: String, b: Int, cc: Long, d: Option[String])
    case class B(a: String, b: Int, c: Int, d: Option[String])
    val a = A("hello", 1, 2, None)
    val b: B = CTransformerBuilder[A, B] // todo `fromField: Long` type Long cannot be ignored.
      .mapField[Long, Int](_.cc, _.c, (fromField: Long) => if (fromField > 0) fromField.toInt else 0)
      .build
      .transform(a)

    b.toString shouldEqual "B(hello,1,2,None)"
//     use implicit

    implicit val transformer = CTransformerBuilder[A, B]
      .mapField(_.b, _.c)
      .mapField(_.a, _.a)
      .mapField[Option[String], Option[String]](_.d, _.d, (map: Option[String]) => map)
      .build

    CTransformer[A, B].transform(a).toString shouldEqual "B(hello,1,1,None)"
  }

  "CTransformerBuilderTest1" should "error if field type is incompatible" in {
    """
      |
      |    case class A(a: String, b: Int, cc: Long, d: Option[String])
      |    case class B(a: String, b: Int, c: Int, d: Option[String])
      |    val a = A("hello", 1, 2, None)
      |    val b: B = CTransformerBuilder[A, B]
      |      .mapField[Long, Int](_.cc, _.c)
      |      .build
      |      .transform(a)
      |""".stripMargin shouldNot compile
  }
}
