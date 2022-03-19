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
object ZRedisConfiguration {

  private val conf: Config = ConfigFactory.load("reference.conf")
  private val custom: Config = ConfigFactory.load("application.conf").withFallback(conf)
  private val redisConf: RedisConfig = RedisConfig(custom.getString("redis.host"), custom.getInt("redis.port"))

  private val codec: ULayer[Has[Codec]] = ZLayer.succeed[Codec](ProtobufCodec)

  val redisLayer: Layer[RedisError.IOError, ZRedisCacheService] =
    ((Logging.ignore ++ ZLayer.succeed(redisConf)) >>>
      RedisExecutor.local ++ ZRedisConfiguration.codec) >>>
      (Redis.live >>> (r => ZRedisLive(r)).toLayer)
}
