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

  "cache2" should "ok while defines a custom cache" in {
    import scala.concurrent.Await
    import scala.concurrent.duration.Duration
    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
    implicit val customCache =
      GenericCache[String, TestEntity](CacheStrategy.CustomCacheStrategy(new CacheAdapter[TestEntity] {
        lazy val underlyingCache: util.HashMap[String, TestEntity] = new util.HashMap[String, TestEntity]()

        override def keys: Set[String] = underlyingCache.keySet().asScala.toSet

        override def batchPut(data: Map[String, TestEntity]): Unit = underlyingCache.putAll(data.asJava)

        override def put(k: String, v: TestEntity): Unit = underlyingCache.put(k, v)

        override def get(k: String): TestEntity = underlyingCache.get(k)

        override def clear(): Unit = underlyingCache.clear()

        override def remove(k: String): Unit = underlyingCache.remove(k)
      }))
    val cache = Cache[String, TestEntity]
    Await.result(cache.batchPutF(data), Duration.Inf)
    val result: Option[TestEntity] = Await.result(cache.getF("btc"), Duration.Inf)
    result shouldBe Some(TestEntity("btc", "hello1", "world1"))

    val result2 = Await.result(cache.getAllF, Duration.Inf)
    result2 shouldBe data
  }
}
