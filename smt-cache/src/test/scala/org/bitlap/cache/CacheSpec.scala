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
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import org.bitlap.common.TestEntity
import scala.concurrent.ExecutionContext

/** @author
 *    梦境迷离
 *  @version 1.0,6/8/22
 */
class CacheSpec extends AnyFlatSpec with Matchers {

  implicit lazy val testEntityAsyncCache =
    GenericCache[String, TestEntity](CacheStrategy.Normal)(ExecutionContext.Implicits.global)

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
    val cache = Cache[String, TestEntity]

    val ret = for {
      _      <- cache.batchPutF(newData)
      btcKey <- cache.getF("btc").map(_.map(_.key))
      _      <- cache.batchPutF(newData2)
      ethKey <- cache.getF("eth").map(_.map(_.key))
    } yield btcKey -> ethKey

    Await.result(ret, 3.seconds) shouldBe Option("btc_key123") -> Option("eth_key456")

  }

  "cache5" should "ok when async cache clear" in {
    val newData = Map(
      "btc"       -> TestEntity("btc", "id123", "btc_key123"),
      "btc-zh-cn" -> TestEntity("btc", "id123", "btc_zh_key123")
    )
    val cache = Cache[String, TestEntity]

    val ret = for {
      _      <- cache.batchPutF(newData)
      btcKey <- cache.getF("btc").map(_.map(_.key))
      _      <- cache.clearF()
      ethKey <- cache.getF("eth").map(_.map(_.key))
    } yield btcKey -> ethKey

    Await.result(ret, 3.seconds) shouldBe Option("btc_key123") -> None

  }

}
