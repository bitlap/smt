package org.bitlap.genericcache

// TODO unit test
object Test extends App {

  val data = Map(
    "btc" -> TestEntity("btc", "hello1", "world1"),
    "etc" -> TestEntity("eth", "hello2", "world2")
  )

  val cache: UnifiedCache[String, TestEntity] = DefaultCacheFactory.createAndInitCache(100, data)

  val result1: Option[TestEntity] = cache.getT("etc")
  println(result1)

  val s: Option[String] = cache.getTField("etc", TestEntity.key)
  println(s)

  cache.refresh(data)

}
case class TestEntity(
  name: String,
  id: String,
  key: String
)
object TestEntity {

  case object id extends CacheField {
    override def stringify: String = "id"

    override type Field = String
  }

  case object key extends CacheField {
    override def stringify: String = "key"
    override type Field = String
  }
}
