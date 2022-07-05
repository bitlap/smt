package org.bitlap.cache

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.util.UUID

/** @author
 *    梦境迷离
 *  @version 1.0,7/5/22
 */
class CacheKeyBuilderSpec extends AnyFlatSpec with Matchers {

  "CacheKeyBuilder1" should "ok while uses uuid type" in {
    val now = UUID.randomUUID()
    val str = CacheKeyBuilder.uuidKey.generateKey(now)

    println(str)

    val v = CacheKeyBuilder.uuidKey.unGenerateKey(str)
    v shouldBe now
  }

}
