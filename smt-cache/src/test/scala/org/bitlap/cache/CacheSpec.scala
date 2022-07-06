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
import org.bitlap.common.CaseClassField

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import org.bitlap.common.TestEntity

/** @author
 *    梦境迷离
 *  @version 1.0,6/8/22
 */
class CacheSpec extends AnyFlatSpec with Matchers {

  private implicit val syncCache  = CacheImplicits.testEntitySyncCache
  private implicit val asyncCache = CacheImplicits.testEntityAsyncCache

  private val data = Map(
    "btc" -> TestEntity("btc", "hello1", "world1"),
    "etc" -> TestEntity("eth", "hello2", "world2")
  )

  "cache1" should "get entity from cache successfully" in {
    val cache = Cache.getSyncCache[String, TestEntity]
    cache.init(data)
    val result: Option[TestEntity] = cache.getT("etc")
    result shouldBe data.get("etc")

    val result2 = cache.getAllT
    result2 shouldBe data
  }

  "cache2" should "get entity's field from cache successfully" in {
    val cache = Cache.getSyncCache[String, TestEntity]
    cache.init(data)
    val result = cache.getTField("etc", CaseClassField[TestEntity](_.key))
    result shouldBe Some("world2")

    val result2 = cache.getTField("etc", CaseClassField[TestEntity](_.key))
    result2 shouldBe Some("world2")
  }

  "cache3" should "get entity's field after refresh" in {
    val cache = Cache.getSyncCache[String, TestEntity]
    cache.init(data)
    val newData = Map(
      "btc"       -> TestEntity("btc", "hello1", "world1"),
      "btc-zh-cn" -> TestEntity("btc", "你好啊", "你好哦"),
      "etc"       -> TestEntity("eth", "hello2", "world2")
    )
    cache.clear()
    cache.putTAll(newData)

    val result: Option[TestEntity] = cache.getT("btc-zh-cn")
    result shouldBe newData.get("btc-zh-cn")
  }

  "cache4" should "async cache" in {
    val newData = Map(
      "btc"       -> TestEntity("btc", "id123", "btc_key123"),
      "btc-zh-cn" -> TestEntity("btc", "id123", "btc_zh_key123")
    )
    val newData2 = Map(
      "btc"       -> TestEntity("btc", "id456", "bt_key456"),
      "btc-zh-cn" -> TestEntity("btc", "id456", "btc_zh_key456"),
      "eth"       -> TestEntity("btc", "id456", "eth_key456")
    )
    val cache = Cache.getAsyncCache[String, TestEntity]

    val ret = for {
      _      <- cache.init(newData)
      btcKey <- cache.getTField("btc", CaseClassField[TestEntity](_.key))
      _      <- cache.putTAll(newData2)
      ethKey <- cache.getTField("eth", CaseClassField[TestEntity](_.key))
    } yield btcKey -> ethKey

    Await.result(ret, 3.seconds) shouldBe Option("btc_key123") -> Option("eth_key456")

  }

  "cache5" should "ok when async cache clear" in {
    val newData = Map(
      "btc"       -> TestEntity("btc", "id123", "btc_key123"),
      "btc-zh-cn" -> TestEntity("btc", "id123", "btc_zh_key123")
    )
    val cache = Cache.getAsyncCache[String, TestEntity]

    val ret = for {
      _      <- cache.init(newData)
      btcKey <- cache.getTField("btc", CaseClassField[TestEntity](_.key))
      _      <- cache.clear()
      ethKey <- cache.getTField("eth", CaseClassField[TestEntity](_.key))
    } yield btcKey -> ethKey

    Await.result(ret, 3.seconds) shouldBe Option("btc_key123") -> None

  }

  "cache6" should "get entity with selectField successfully" in {
    val cache = Cache.getSyncCache[String, TestEntity]
    cache.init(data)

    val id: Identity[Option[CaseClassField#Field]] =
      cache.getTField("etc", CaseClassField[TestEntity](_.id))
    println(id)
    id shouldBe data.get("etc").map(_.id)

    val value =
      cache.getTField("etc", CaseClassField[TestEntity](_.value))
    println(value)
    value shouldBe data.get("etc").map(_.value)
  }
}
