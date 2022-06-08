package org.bitlap.genericcache

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.bitlap.genericcache.CacheType.Normal

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
    val result: Option[TestEntity]          = cache.getT("etc")
    result shouldBe data.get("etc")
  }

  "cache2" should "get entity's field from cache successfully" in {
    val cache: CacheRef[String, TestEntity] = DefaultCacheFactory.createCache(Normal)
    cache.init(data)
    val result: Option[String]              = cache.getTField("etc", TestEntity.key)
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
