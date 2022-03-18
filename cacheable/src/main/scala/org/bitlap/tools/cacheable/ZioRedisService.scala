/*
 * Copyright (c) 2022 org.bitlap
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

package org.bitlap.tools.cacheable

import zio.{ IO, Layer, ZIO }
import zio.redis.RedisError
import zio.schema.Schema

/**
 * Redis service.
 *
 * @author 梦境迷离
 * @version 2.0,2022/1/10
 */
trait ZioRedisService {

  /**
   * @param key
   * @param field
   * @return Long
   */
  def hDel(key: String, field: String): ZIO[ZRedisCacheService, RedisError, Long]

  /**
   *
   * @param key
   * @param field
   * @param value
   * @return Long
   */
  def hSet[T: Schema](key: String, field: String, value: T): ZIO[ZRedisCacheService, RedisError, Long]

  /**
   *
   * @param key
   * @param field
   * @return Option[T]
   */
  def hGet[T: Schema](key: String, field: String): ZIO[ZRedisCacheService, RedisError, Option[T]]

  /**
   *
   * @param key
   * @return Long
   */
  def exists(key: String): ZIO[ZRedisCacheService, RedisError, Long]

}

object ZioRedisService {

  implicit val zioRedisLayer: Layer[RedisError.IOError, ZRedisCacheService] = ZioRedisConfiguration.redisLayer

  def hDel(key: String, field: String)(implicit layer: Layer[RedisError.IOError, ZRedisCacheService]): IO[RedisError, Long] =
    ZIO.serviceWith[ZioRedisService](_.hDel(key, field)).provideLayer(layer)

  def hSet[T: Schema](key: String, field: String, value: T)(implicit layer: Layer[RedisError.IOError, ZRedisCacheService]): IO[RedisError, Long] =
    ZIO.serviceWith[ZioRedisService](_.hSet[T](key, field, value)).provideLayer(layer)

  def hGet[T: Schema](key: String, field: String)(implicit layer: Layer[RedisError.IOError, ZRedisCacheService]): IO[RedisError, Option[T]] =
    ZIO.serviceWith[ZioRedisService](_.hGet(key, field)).provideLayer(layer)

  def exists(key: String)(implicit layer: Layer[RedisError.IOError, ZRedisCacheService]): IO[RedisError, Long] =
    ZIO.serviceWith[ZioRedisService](_.exists(key)).provideLayer(layer)
}
