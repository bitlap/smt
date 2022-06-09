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
}
