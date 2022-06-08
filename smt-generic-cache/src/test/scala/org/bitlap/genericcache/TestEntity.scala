package org.bitlap.genericcache

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
