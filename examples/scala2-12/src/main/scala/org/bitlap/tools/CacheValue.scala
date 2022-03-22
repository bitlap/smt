package org.bitlap.tools
import zio.schema.{ DeriveSchema, Schema }

/**
 *
 * @author 梦境迷离
 * @version 1.0,2022/3/22
 */
case class CacheValue(i: String)

object CacheValue {

  implicit val cacheValueSchema: Schema[CacheValue] = DeriveSchema.gen[CacheValue]
}
