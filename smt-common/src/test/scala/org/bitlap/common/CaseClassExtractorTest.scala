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

package org.bitlap.common

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** @author
 *    梦境迷离
 *  @version 1.0,6/8/22
 */
class CaseClassExtractorTest extends AnyFlatSpec with Matchers {

  "CaseClassExtractorTest1" should "safe" in {
    val obj = TestEntity("name", "id", "key", Some(1))
    val key: Option[String] =
      CaseClassExtractor.getFieldValueSafely[TestEntity, TestEntity.key.Field](obj, TestEntity.key.stringify)
    assert(key == Option("key"))
  }

  "CaseClassExtractorTest2" should "error" in {
    """
      | val obj = TestEntity("name", "id", "key", Some(1))
      |    val key2: Option[Nothing] = CaseClassExtractor.getField(obj, TestEntity.key.stringify)
      |    assert(key2 == Option("key"))
      |""".stripMargin shouldNot compile
  }

  "CaseClassExtractorTest3" should "unsafe" in {
    val obj                 = TestEntity("name", "id", "key", Some(1))
    val key: Option[String] = CaseClassExtractor.getFieldValueUnSafely(obj, TestEntity.key)
    assert(key == Option("key"))
  }

  "CaseClassExtractorTest4" should "selectField" in {
    val key: CaseClassField = CaseClassField[TestEntity]("key")
    assert(key.stringify == "key")
    val value: CaseClassField = CaseClassField[TestEntity]("value")
    assert(value.stringify == "value")
  }

  "CaseClassExtractorTest5" should "error in case class with curry" in {
    """
      |    import org.bitlap.common.CaseClassField.fieldOf
      |    case class TestEntity2(
      |      name: String,
      |      id: String,
      |      key: String,
      |      value: Option[Int] = None
      |    )(i: Int)
      |    val key: CaseClassField = fieldOf[TestEntity2]("key")
      |""".stripMargin shouldNot compile
  }
}
