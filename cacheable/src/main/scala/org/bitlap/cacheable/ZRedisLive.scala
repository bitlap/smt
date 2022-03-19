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

package org.bitlap.cacheable

import zio.redis.{ Redis, RedisError }
import zio.schema.Schema
import zio.{ Has, ULayer, ZIO, ZLayer, redis }

/**
 * @author 梦境迷离
 * @see https://zio.dev/version-1.x/datatypes/contextual/#module-pattern-20
 * @version 2.0,2022/1/17
 */
case class ZRedisLive(private val rs: Redis) extends ZRedisService {

  private lazy val redisLayer: ULayer[Has[Redis]] = ZLayer.succeed(rs)

  override def del(key: String): ZIO[ZRedisCacheService, RedisError, Long] =
    redis.del(key).orDie.provideLayer(redisLayer)

  override def hSet[T: Schema](key: String, field: String, value: T): ZIO[ZRedisCacheService, RedisError, Long] =
    redis.hSet[String, String, T](key, field -> value).provideLayer(redisLayer)

  override def hGet[T: Schema](key: String, field: String): ZIO[ZRedisCacheService, RedisError, Option[T]] =
    redis.hGet(key, field).returning[T].provideLayer(redisLayer)

  override def exists(key: String): ZIO[ZRedisCacheService, RedisError, Long] =
    redis.exists(key).provideLayer(redisLayer)

}
