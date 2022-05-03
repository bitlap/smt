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

package org.bitlap.cacheable.redis

import zio.redis.RedisError
import zio.schema.Schema
import zio.{ IO, Layer, ZIO }

/**
 * Redis service.
 *
 * @author 梦境迷离
 * @version 2.0,2022/1/10
 */
trait ZRedisService {

  /**
   * @param key
   * @return Long
   */
  def del(key: String): ZIO[ZRedisCacheService, RedisError, Long]

  /**
   * @param key
   * @param field
   * @param value
   * @return Long
   */
  def hSet[T: Schema](key: String, field: String, value: T): ZIO[ZRedisCacheService, RedisError, Long]

  /**
   * @param key
   * @param field
   * @return Option[T]
   */
  def hGet[T: Schema](key: String, field: String): ZIO[ZRedisCacheService, RedisError, Option[T]]

  /**
   * @param key
   * @tparam T
   * @return
   */
  def hGetAll[T: Schema](key: String): ZIO[ZRedisCacheService, RedisError, Map[String, T]]

}

object ZRedisService {

  lazy val zioRedisLayer: Layer[RedisError.IOError, ZRedisCacheService] = ZRedisConfiguration.redisLayer

  def del(key: String)(implicit
    layer: Layer[RedisError.IOError, ZRedisCacheService] = zioRedisLayer
  ): IO[RedisError, Long] =
    ZIO.serviceWith[ZRedisService](_.del(key)).provideLayer(layer)

  def hSet[T: Schema](key: String, field: String, value: T)(implicit
    layer: Layer[RedisError.IOError, ZRedisCacheService] = zioRedisLayer
  ): IO[RedisError, Long] =
    ZIO.serviceWith[ZRedisService](_.hSet[T](key, field, value)).provideLayer(layer)

  def hGet[T: Schema](key: String, field: String)(implicit
    layer: Layer[RedisError.IOError, ZRedisCacheService] = zioRedisLayer
  ): IO[RedisError, Option[T]] =
    ZIO.serviceWith[ZRedisService](_.hGet[T](key, field)).provideLayer(layer)
}
