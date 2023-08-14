/*
 * Copyright (c) 2023 bitlap
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

package org.bitlap.common

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** @author
 *    梦境迷离
 *  @version 1.0,6/15/22
 */
class TransformableTest extends AnyFlatSpec with Matchers {

  "TransformableTest simple case" should "ok for Transformable" in {
    case class A1(a: String, b: Int, cc: Long, d: Option[String])
    case class A2(a: String, b: Int, c: Int, d: Option[String])

    val a = A1("hello", 1, 2, None)
    val b: A2 = Transformable[A1, A2]
      .setName(_.cc, _.c)
      .setType[Long, Int](_.cc, fromField => if (fromField > 0) fromField.toInt else 0)
      .instance
      .transform(a)

    b.toString shouldEqual "A2(hello,1,2,None)"

    case class B1(d: List[String])
    case class B2(d: Seq[String])

    val b1 = B1(List("hello"))
    // List => Seq  not need mapping field
    val b2: B2 = Transformable[B1, B2].instance.transform(b1)
    b2.toString shouldEqual "B2(List(hello))"
  }

  "TransformableTest simple case, name is equals, but type not" should "ok for Transformable" in {
    case class A1(a: String)
    case class A2(a: Int)

    val a = A1("1112")
    val b: A2 = Transformable[A1, A2]
      .setType[String, Int](_.a, _.toInt)
      .instance
      .transform(a)

    b.toString shouldEqual "A2(1112)"
  }

  "TransformableTest simple case" should "ok for implicit Transformable" in {
    case class A1(a: String, b: Int, cc: Long, d: Option[String])
    case class A2(a: String, b: Int, c: Int, d: Option[String])
    val a = A1("hello", 1, 2, None)
    implicit val transformer = Transformable[A1, A2]
      .setName(_.b, _.c)
      .setName(_.a, _.a)
      .setType[Option[String], Option[String]](_.d, (map: Option[String]) => map)
      .instance

    Transformer[A1, A2].transform(a).toString shouldEqual "A2(hello,1,1,None)"
  }

  "TransformableTest type not match" should "error if field type is incompatible" in {
    """
      |
      |    case class A1(a: String, b: Int, cc: Long, d: Option[String])
      |    case class A2(a: String, b: Int, c: Int, d: Option[String])
      |    val a = A1("hello", 1, 2, None)
      |    val b: A2 = Transformable[A1, A2]
      |      .setName(_.cc, _.c)
      |      .instance
      |      .transform(a)
      |""".stripMargin shouldNot compile
  }

  "TransformableTest simple case for nest field" should "ok when field is case class" in {
    case class C1(j: Int)
    case class D1(c1: C1)
    case class C2(j: Int)
    case class D2(c2: C2)

    implicit val cTransformer: Transformer[C1, C2] = Transformable[C1, C2].instance
    implicit val dTransformer: Transformer[D1, D2] = Transformable[D1, D2].setName(_.c1, _.c2).instance

    val d1     = D1(C1(1))
    val d2: D2 = Transformer[D1, D2].transform(d1)
    println(d2)
  }

  "TransformableTest more complex case for nest field" should "ok when field is list with case class" in {
    case class C1(j: Int)
    case class D1(c1: List[C1])
    case class C2(j: Int)
    case class D2(c2: List[C2])

    implicit val cTransformer: Transformer[C1, C2] = Transformable[C1, C2].instance
    implicit val dTransformer: Transformer[D1, D2] = Transformable[D1, D2].setName(_.c1, _.c2).instance

    val d1     = D1(List(C1(1), C1(2)))
    val d2: D2 = Transformer[D1, D2].transform(d1)
    println(d2)
  }

  "TransformableTest more complex case for two-layer nest field" should "ok for implicit and non-implicit(setName)" in {
    case class C1(j: Int)
    case class D1(c1: List[List[C1]])

    case class C2(j: Int)
    case class D2(c2: List[List[C2]]) // Nesting of the second layer

    object C1 {
      implicit val cTransformer: Transformer[C1, C2] = Transformable[C1, C2].instance
    }

    object D1 {
      implicit val dTransformer: Transformer[D1, D2] = Transformable[D1, D2]
        .setName(_.c1, _.c2)
        .setType[List[List[C1]], List[List[C2]]](
          _.c1,
          _.map(_.map(Transformer[C1, C2].transform(_)))
        )
        .instance
    }

    val d1     = D1(List(List(C1(1), C1(2))))
    val d2: D2 = Transformer[D1, D2].transform(d1)
    d2.toString shouldBe "D2(List(List(C2(1), C2(2))))"
  }

  "TransformableTest more complex case for two-layer nest field" should "ok for implicit automatically" in {
    case class C1(j: Int)
    case class D1(
      a1: List[List[C1]],
      b1: Option[C1],
      c1: List[Option[C1]],
      d1: Option[List[C1]],
      map1: Map[String, String],
      intMap1: Map[Int, C1]
    )

    case class C2(j: Int)
    case class D2(
      a2: List[List[C2]],
      b2: Option[C1],
      c2: List[Option[C1]],
      d2: Option[List[C1]],
      map2: Map[String, String],
      intMap2: Map[Int, C2]
    )

    // NOTE: have collection not support? please implicit val transformer = new Transformer[F, T] { ... }

    object C1 {
      implicit val cTransformer: Transformer[C1, C2] = Transformable[C1, C2].instance
    }

    object D1 {
      implicit val dTransformer: Transformer[D1, D2] = Transformable[D1, D2]
        .setName(_.a1, _.a2)
        .setName(_.b1, _.b2)
        .setName(_.c1, _.c2)
        .setName(_.d1, _.d2)
        .setName(_.map1, _.map2)
        .setName(_.intMap1, _.intMap2)
        .instance
    }

    val d1 = D1(
      List(List(C1(1))),
      Option(C1(2)),
      List(Option(C1(3))),
      Option(List(C1(4))),
      Map("hello" -> "world"),
      Map(1       -> C1(1))
    )

    val d2: D2 = Transformer[D1, D2].transform(d1)

    println(d2)

    d2.toString shouldBe "D2(List(List(C2(1))),Some(C1(2)),List(Some(C1(3))),Some(List(C1(4))),Map(hello -> world),Map(1 -> C2(1)))"

    d1.transform[D2]
      .toString shouldBe "D2(List(List(C2(1))),Some(C1(2)),List(Some(C1(3))),Some(List(C1(4))),Map(hello -> world),Map(1 -> C2(1)))"

  }

  "TransformableTest different type" should "compile ok if can use weak conformance" in {
    case class A1(a: String, b: Int, cc: Int, d: Option[String]) // weak conformance
    case class A2(a: String, b: Int, c: Long, d: Option[String])
    object A1 {
      implicit val aTransformer: Transformer[A1, A2] = Transformable[A1, A2].setName(_.cc, _.c).instance
    }
    val a1 = A1("hello", 1, 2, None)
    val a2 = Transformer[A1, A2].transform(a1)
    a2.toString shouldBe "A2(hello,1,2,None)"

  }

  "TransformableTest type cannot match" should "compile failed if can't use weak conformance" in {
    """
      | case class A1(a: String, b: Int, cc: Long, d: Option[String]) // Can't to use weak conformance, must use `setName(?,?,?)` method for it.
      |    case class A2(a: String, b: Int, c: Int, d: Option[String])
      |    object A1 {
      |      
      |      implicit val aTransformer: Transformer[A1, A2] = Transformable[A1, A2].setName(_.cc,_.c).instance
      |    }
      |    val a1 = A1("hello", 1, 2, None)
      |    val a2 = Transformer[A1, A2].transform(a1)
      |    a2.toString shouldBe "A2(hello,1,2,None)"
      |""".stripMargin shouldNot compile
  }

  "TransformableTest more complex case to use implicit Transformer" should "compile ok" in {
    import org.bitlap.common.models.from._
    import org.bitlap.common.models.to._
    val fromRow =
      List(FRow(List("this is row data1", "this is row data2")))
    val fromRowSet      = FRowSet.apply(fromRow, 100000)
    val fromColumnDesc  = List(FColumnDesc("this is column name1"), FColumnDesc("this is column name2"))
    val fromTableSchema = FTableSchema(fromColumnDesc)
    val fromQueryResult = FQueryResult(tableSchema = fromTableSchema, rows = fromRowSet)

    val toRow =
      List(TRow(List("this is row data1", "this is row data2")))
    val toRowSet            = TRowSet(100000, toRow)
    val toColumnDesc        = List(TColumnDesc("this is column name1"), TColumnDesc("this is column name2"))
    val toTableSchema       = TTableSchema(toColumnDesc)
    val expectToQueryResult = TQueryResult(ttableSchema = toTableSchema, trows = toRowSet)

    val actualToQueryResult = Transformer[FQueryResult, TQueryResult].transform(fromQueryResult)

    actualToQueryResult shouldBe expectToQueryResult
  }

  "TransformableTest From have fewer fields than To" should "compile error" in {
    """
      |    case class B1(a: List[String])
      |    case class B2(a: List[String], b: Int)
      |    val b2 = Transformable[B1, B2].instance.transform(B1(List.empty))
      |    println(b2)
      |""".stripMargin shouldNot compile
  }

  "TransformableTest From have more fields than To" should "ok" in {
    case class B1(a: List[String], b: Int)
    case class B2(a: List[String])
    val b2 = Transformable[B1, B2].instance.transform(B1(List.empty, 1))
    println(b2)
  }

  "TransformableTest assign list to seq" should "ok for implicit automatically" in {
    case class C1(j: Int)
    case class D1(
      a1: List[C1]
    )

    case class C2(j: Int)
    case class D2(
      a2: Seq[C2]
    )

    object C1 {
      implicit val cTransformer: Transformer[C1, C2] = Transformable[C1, C2].instance
    }

    object D1 {

      implicit val dTransformer: Transformer[D1, D2] = Transformable[D1, D2]
        .setName(_.a1, _.a2)
        .instance
    }

    val d1 = D1(
      List(C1(1))
    )

    val d2: D2 = Transformer[D1, D2].transform(d1)

    println(d2)

    d2.toString shouldBe "D2(List(C2(1)))"
  }

  "TransformableTest support set and vector" should "ok for implicit automatically" in {
    case class C1(j: Int)
    case class D1(
      a1: Seq[C1],
      b1: Set[C1],
      c1: Vector[C1]
    )

    case class C2(j: Int)
    case class D2(
      a2: List[C2],
      b2: Set[C2],
      c2: Vector[C2]
    )

    object C1 {
      implicit val cTransformer: Transformer[C1, C2] = Transformable[C1, C2].instance
    }

    object D1 {

      implicit val dTransformer: Transformer[D1, D2] = Transformable[D1, D2]
        .setName(_.a1, _.a2)
        .setName(_.b1, _.b2)
        .setName(_.c1, _.c2)
        .instance
    }

    val d1 = D1(
      Seq(C1(1)),
      Set.empty,
      Vector.empty
    )

    val d2: D2 = Transformer[D1, D2].transform(d1)

    println(d2)

    d2.toString shouldBe "D2(List(C2(1)),Set(),Vector())"
  }

  "TransformableTest ops from object" should "ok" in {
    case class A1(a: String, b: Int, cc: Long, d: Option[String])
    case class A2(a: String, b: Int, c: Int, d: Option[String])

    val a = A1("hello", 1, 2, None)

    implicit val b = Transformable[A1, A2]
      .setName(_.cc, _.c)
      .setType[Long, Int](_.cc, fromField => if (fromField > 0) fromField.toInt else 0)
      .instance

    a.transform[A2].toString shouldEqual "A2(hello,1,2,None)"

  }

  "TransformableTest setDefaultValue" should "ok" in {
    case class A1(a: String, b: Int, cc: Long)
    case class A2(a: String, b: Int, c: Int, d: Option[String])

    val a = A1("hello", 1, 2)

    implicit val b: Transformer[A1, A2] = Transformable[A1, A2]
      .setName(_.cc, _.c)
      .setType[Long, Int](_.cc, fromField => fromField.toInt)
      .setDefaultValue(_.d, None)
      .instance

    a.transform[A2].toString shouldEqual "A2(hello,1,2,None)"
  }

  "TransformableTest not setDefaultValue" should "compile failed" in {
    """
      |    case class A1(a: String, b: Int, cc: Long)
      |    case class A2(a: String, b: Int, c: Int, d: Option[String])
      |
      |    val a = A1("hello", 1, 2)
      |
      |    implicit val b: Transformer[A1, A2] = Transformable[A1, A2]
      |      .setName(_.cc, _.c)
      |      .setType[Long, Int](_.cc, fromField => fromField.toInt)
      |      .instance
      |""".stripMargin shouldNot compile
  }

  "TransformableTest enable* method" should "ok" in {
    case class A1(a: String, b: Int, cc: Long)
    case class A2(
      a: String,
      b: Int,
      c: Int,
      d: Option[String],
      e: List[String],
      f: Seq[String],
      g: Set[String],
      h: Vector[String]
    )

    val a = A1("hello", 1, 2)

    implicit val b: Transformer[A1, A2] = Transformable[A1, A2]
      .setName(_.cc, _.c)
      .setType[Long, Int](_.cc, fromField => fromField.toInt)
      .enableOptionDefaultsToNone
      .enableCollectionDefaultsToEmpty
      .instance

    a.transform[A2](b).toString shouldEqual "A2(hello,1,2,None,List(),List(),Set(),Vector())"

    implicit val b2: Transformer[A1, A2] = Transformable[A1, A2]
      .setName(_.cc, _.c)
      .setType[Long, Int](_.cc, fromField => fromField.toInt)
      .setDefaultValue[Vector[String]](
        _.h,
        Vector("Hello world")
      ) // Higher priority than enableCollectionDefaultsToEmpty
      .enableCollectionDefaultsToEmpty
      .enableOptionDefaultsToNone
      .instance

    a.transform[A2](b2).toString shouldEqual "A2(hello,1,2,None,List(),List(),Set(),Vector(Hello world))"

    implicit val b3: Transformer[A1, A2] = Transformable[A1, A2]
      .setName(_.cc, _.c)
      .setType[Long, Int](_.cc, fromField => fromField.toInt)
      .setDefaultValue[Option[String]](_.d, Option("Hello world")) // Higher priority than enableOptionDefaultsToNone
      .enableCollectionDefaultsToEmpty
      .instance

    a.transform[A2](b3).toString shouldEqual "A2(hello,1,2,Some(Hello world),List(),List(),Set(),Vector())"

    implicit val b4: Transformer[A1, A2] = Transformable[A1, A2]
      .setName(_.cc, _.c)
      .setType[Long, Int](_.cc, fromField => fromField.toInt)
      .setDefaultValue[Vector[String]](
        _.h,
        Vector("Hello world1")
      ) // Higher priority than enableCollectionDefaultsToEmpty
      .setDefaultValue[Option[String]](_.d, Option("Hello world2")) // Higher priority than enableOptionDefaultsToNone
      .enableOptionDefaultsToNone
      .enableCollectionDefaultsToEmpty
      .instance

    a.transform[A2](b4).toString shouldEqual "A2(hello,1,2,Some(Hello world2),List(),List(),Set(),Vector(Hello world1))"

  }

  "TransformableTest disable* method" should "ok" in {
    case class A1(d: Option[String])
    case class A2(
      d: Option[String],
      e: Option[String] = Some("option"),
      f: Option[String] = None,
      h: List[String] = List("list"),
      i: List[String] = List.empty
    )

    val a = A1(Some("hello a"))
    implicit val b1: Transformer[A1, A2] =
      Transformable[A1, A2].enableCollectionDefaultsToEmpty.enableOptionDefaultsToNone.instance

    a.transform[A2](b1).toString shouldEqual "A2(Some(hello a),None,None,List(),List())"

    implicit val b2: Transformer[A1, A2] =
      Transformable[A1, A2]
        // This method has a higher priority
        .setDefaultValue(_.f, Option("1"))
        .disableCollectionDefaultsToEmpty // use default value, not None
        .disableOptionDefaultsToNone      // use default value, not Empty
        .instance

    a.transform[A2](b2).toString shouldEqual "A2(Some(hello a),Some(option),Some(1),List(list),List())"

  }

  "TransformableTest disable* is ok" should "compile ok" in {
    case class A1(d: Option[String])
    case class A2(
      d: Option[String],
      e: Option[String] = Some("option"),
      f: Option[String] = None,
      h: List[String] = List("list"),
      i: List[String] = List.empty
    )

    val a                                = A1(Some("hello a"))
    implicit val b1: Transformer[A1, A2] = Transformable[A1, A2].instance

    a.transform[A2](b1).toString shouldEqual "A2(Some(hello a),Some(option),None,List(list),List())"
  }

  "TransformableTest disable* is ok if no default value" should "compile failed" in {
    """
      |    case class A1(d: Option[String])
      |    case class A2(
      |      d: Option[String],
      |      e: Option[String],
      |      f: Option[String] = None,
      |      h: List[String] = List("list"),
      |      i: List[String] = List.empty,
      |    )
      |
      |    val a = A1(Some("hello a"))
      |    implicit val b1: Transformer[A1, A2] = Transformable[A1, A2].instance
      |
      |    a.transform[A2](b1)
      |""".stripMargin shouldNot compile
  }

  "TransformableTest extends TransformableOps" should "compile ok" in {
    case class A1(a: Option[String], b: Boolean)

    object A1 extends TransformableSyntax[A1, A2] {
      override val transformer: Transformer[A1, A2] =
        Syntax
          .setName(_.a, _.b)
          .setName(_.b, _.c)
          .setType[Boolean, String](_.b, _.toString)
          .setDefaultValue(_.b, None)
          .instance
    }

    case class A2(
      b: Option[String],
      c: String,
      e: Option[String] = Some("option")
    )

    val a = A1(Some("hello a"), false)
    a.transformCaseClass[A2].toString shouldEqual "A2(Some(hello a),false,Some(option))"
  }

  "TransformableTest extends TransformableOps" should "ok" in {
    case class A1(d: Option[String])

    object A1 extends TransformableSyntax[A1, A2] {
      override val transformer: Transformer[A1, A2] =
        Syntax
          .setDefaultValue(_.f, Option("1"))
          .disableCollectionDefaultsToEmpty // use default value, not None
          .disableOptionDefaultsToNone      // use default value, not Empty
          .instance

    }
    case class A2(
      d: Option[String],
      e: Option[String] = Some("option"),
      f: Option[String] = None,
      h: List[String] = List("list"),
      i: List[String] = List.empty
    )

    val a = A1(Some("hello a"))

    a.transformCaseClass[A2].toString shouldEqual "A2(Some(hello a),Some(option),Some(1),List(list),List())"

  }
}
