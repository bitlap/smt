package org.bitlap.genericcache

import org.bitlap.common.CaseClassField

case class TestEntity(
  name: String,
  id: String,
  key: String
)

object TestEntity {

  case object id extends CaseClassField {
    override def stringify: String = "id"

    override type Field = String
  }

  case object key extends CaseClassField {
    override def stringify: String = "key"

    override type Field = String
  }
}
