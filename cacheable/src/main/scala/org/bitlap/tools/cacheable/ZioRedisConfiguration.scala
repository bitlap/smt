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

import com.typesafe.config.{ Config, ConfigFactory }
import zio._
import zio.logging.Logging
import zio.redis.{ Redis, RedisConfig, RedisError, RedisExecutor }
import zio.schema.codec.{ Codec, ProtobufCodec }

/**
 * redis configuration
 *
 * @author 梦境迷离
 * @since 2022/1/10
 * @version 2.0
 */
object ZioRedisConfiguration {

  private val conf: Config = ConfigFactory.load()
  private val custom: Config = ConfigFactory.load("application.conf")

  private val redisConf: RedisConfig =
    if (custom.isEmpty) {
      RedisConfig(conf.getString("redis.host"), conf.getInt("redis.port"))
    } else {
      RedisConfig(custom.getString("redis.host"), custom.getInt("redis.port"))
    }

  private val codec: ULayer[Has[Codec]] = ZLayer.succeed[Codec](ProtobufCodec)

  val redisLayer: Layer[RedisError.IOError, ZRedisCacheService] =
    ((Logging.ignore ++ ZLayer.succeed(redisConf)) >>>
      RedisExecutor.local ++ ZioRedisConfiguration.codec) >>>
      (Redis.live >>> (r => ZioRedisLive(r)).toLayer)
}
