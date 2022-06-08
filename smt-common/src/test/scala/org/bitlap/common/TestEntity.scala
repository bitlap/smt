package org.bitlap.common

/** @author
 *    梦境迷离
 *  @version 1.0,6/8/22
 */
case class TestEntity(
  name: String,
  id: String,
  key: String,
  value: Option[Int] = None
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

  case object value extends CaseClassField {
    override type Field = Option[String]

    override def stringify: String = "value"
  }
}
