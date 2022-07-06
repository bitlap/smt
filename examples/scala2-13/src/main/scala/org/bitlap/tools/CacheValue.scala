package org.bitlap.tools

import zio.schema.{ DeriveSchema, Schema }

/** @author
 *    梦境迷离
 *  @version 1.0,2022/3/22
 */

// The case class should be here, not in the test
case class CacheValue(i: String)

object CacheValue {

  implicit val cacheValueSchema: Schema[CacheValue] = DeriveSchema.gen[CacheValue]
}
