/*
 * Copyright (c) 2022 bitlap
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

import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** @author
 *    梦境迷离
 *  @version 1.0,2021/8/3
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

  object EnumType3 extends Enumeration {
    type EnumType3 = Value
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
    case class A(enum1: EnumType.EnumType, enum2: EnumType.EnumType = EnumType.A, i: Int)
  }

  "jacksonEnum3" should "ok" in {
    @jacksonEnum
    case class A(var enum1: EnumType.EnumType, enum2: EnumType2.EnumType2 = EnumType2.A, i: Int)
    @jacksonEnum(nonTypeRefers = Seq("EnumType", "EnumType2")) // Because it has been created
    class B(
      var enum1: EnumType.EnumType, // No annotation will add
      val enum2: EnumType2.EnumType2 = EnumType2.A,
      val enum3: EnumType3.EnumType3,
      i: Int
    )
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

  "jacksonEnum6" should "failed when input args are invalid" in {
    """
      |    @jacksonEnum(nonTypeRefers=Nil)
      |    class A(enum1: EnumType.EnumType)
      |""".stripMargin should compile
  }
}
