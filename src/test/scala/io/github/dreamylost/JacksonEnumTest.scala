package io.github.dreamylost

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration

/**
 *
 * @author li.guobin@immomo.com
 * @version 1.0,2021/8/3
 */
class JacksonEnumTest extends AnyFlatSpec with Matchers {

  object EnumType extends Enumeration {
    type EnumType = Value
    val A = Value(1)
    val B = Value(2)
  }

  object EnumType2 extends Enumeration {
    type EnumType2 = Value
    val A, B = Value
  }

  "jacksonEnum1" should "ok" in {
    class EnumTypeTypeRefer extends _root_.com.fasterxml.jackson.core.`type`.TypeReference[EnumType.type]
    case class A(
      @JsonScalaEnumeration(classOf[EnumTypeTypeRefer]) enum1: EnumType.EnumType,
      enum2: EnumType.EnumType = EnumType.A
    )
  }

  "jacksonEnum2" should "ok" in {
    @jacksonEnum
    case class A(
      enum1: EnumType.EnumType,
      enum2: EnumType.EnumType = EnumType.A,
      i: Int)
  }

  "jacksonEnum3" should "ok" in {
    @jacksonEnum
    case class A(
      var enum1: EnumType.EnumType,
      enum2: EnumType2.EnumType2 = EnumType2.A,
      i: Int)

    @jacksonEnum(nonTypeRefers = Seq("EnumType", "EnumType2")) // Because it has been created 
    class B(
      var enum1: EnumType.EnumType,
      enum2: EnumType2.EnumType2 = EnumType2.A,
      i: Int)
  }

  "jacksonEnum4" should "ok when duplication" in {
    """
      |    @jacksonEnum
      |    case class A(
      |      @JsonScalaEnumeration(classOf[EnumTypeTypeRefer]) var enum1: EnumType.EnumType,
      |      enum2: EnumType2.EnumType2 = EnumType2.A,
      |      i: Int)
      |""".stripMargin should compile

    """
      |    @jacksonEnum
      |    class A(
      |      @JsonScalaEnumeration(classOf[EnumTypeTypeRefer]) var enum1: EnumType.EnumType,
      |      enum2: EnumType2.EnumType2 = EnumType2.A,
      |      i: Int)
      |""".stripMargin should compile
  }

  "jacksonEnum5" should "failed on object" in {
    """
      |    @jacksonEnum
      |    object A()
      |""".stripMargin shouldNot compile
  }

}
