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

package org.bitlap.cache

import java.util
import scala.jdk.CollectionConverters._
import org.bitlap.common.TestEntity
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** @author
 *    梦境迷离
 *  @version 1.0,7/5/22
 */
class CacheCustomCaseSpec extends AnyFlatSpec with Matchers {

  private val data = Map(
    "btc"  -> TestEntity("btc", "hello1", "world1"),
    "etc1" -> TestEntity("eth1", "hello1", "world2"),
    "etc2" -> TestEntity("eth2", "hello2", "world2")
  )

  "cache1" should "ok while uses lru cache" in {
    implicit val lruCache = CacheImplicits.testEntitySyncLruCache
    val cache             = Cache.getSyncCache[String, TestEntity]
    cache.init(data)

    cache.putT("etc3", TestEntity("eth3", "hello3", "world2"))

    val result: Option[TestEntity] = cache.getT("btc")
    result shouldBe None
  }

  "cache2" should "ok while defines a custom cache" in {
    implicit val customCache =
      GenericCache[String, TestEntity](CacheStrategy.CustomCacheStrategy(new CacheAdapter[TestEntity] {
        lazy val underlyingCache: util.HashMap[String, TestEntity] = new util.HashMap[String, TestEntity]()

        override def getAllKeys: Set[String] = underlyingCache.keySet().asScala.toSet

        override def putAll(map: Map[String, TestEntity]): Unit = underlyingCache.putAll(map.asJava)

        override def put(k: String, v: TestEntity): Unit = underlyingCache.put(k, v)

        override def get(k: String): TestEntity = underlyingCache.get(k)

        override def clear(): Unit = underlyingCache.clear()
      }))
    val cache = Cache.getSyncCache[String, TestEntity]
    cache.init(data)
    val result: Option[TestEntity] = cache.getT("btc")
    result shouldBe Some(TestEntity("btc", "hello1", "world1"))
  }
}
