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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.bitlap.cache.CacheType.Normal

/** @author
 *    梦境迷离
 *  @version 1.0,6/8/22
 */
class CacheSpec extends AnyFlatSpec with Matchers {

  private val data = Map(
    "btc" -> TestEntity("btc", "hello1", "world1"),
    "etc" -> TestEntity("eth", "hello2", "world2")
  )

  "cache1" should "get entity from cache successfully" in {
    val cache: CacheRef[String, TestEntity] = DefaultCacheFactory.createCache(Normal)
    cache.init(data)
    val result: Option[TestEntity] = cache.getT("etc")
    result shouldBe data.get("etc")
  }

  "cache2" should "get entity's field from cache successfully" in {
    val cache: CacheRef[String, TestEntity] = DefaultCacheFactory.createCache(Normal)
    cache.init(data)
    val result: Option[String] = cache.getTField("etc", TestEntity.key)
    result shouldBe Some("world2")
  }

  "cache3" should "get entity's field after refresh" in {
    val cache: CacheRef[String, TestEntity] = DefaultCacheFactory.createCache(Normal)
    cache.init(data)
    val newData = Map(
      "btc"       -> TestEntity("btc", "hello1", "world1"),
      "btc-zh-cn" -> TestEntity("btc", "你好啊", "你好哦"),
      "etc"       -> TestEntity("eth", "hello2", "world2")
    )
    cache.putTAll(newData)

    val result: Option[TestEntity] = cache.getT("btc-zh-cn")
    result shouldBe newData.get("btc-zh-cn")
  }
}
