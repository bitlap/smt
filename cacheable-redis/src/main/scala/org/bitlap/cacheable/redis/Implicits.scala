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

import org.bitlap.cacheable.core.{ LogUtils, ZIOCache, ZIOUpdateCache, ZStreamCache, ZStreamUpdateCache }
import zio.ZIO
import zio.schema.Schema
import zio.stream.ZStream

/**
 * redis cache
 *
 * @author 梦境迷离
 * @version 1.0,2022/3/21
 */
object Implicits {

  implicit def StreamUpdateCache[T: Schema]: ZStreamUpdateCache[Any, Throwable, T] = new ZStreamUpdateCache[Any, Throwable, T] {
    override def evict(business: => ZStream[Any, Throwable, T])(identities: List[String]): ZStream[Any, Throwable, T] = {
      for {
        updateResult <- ZStream.fromIterable(identities).map(key => ZRedisService.del(key)) *> business
        _ <- if (ZRedisConfiguration.disabledLog) ZStream.unit else LogUtils.debugS(s"Redis ZStream update: identities:[$identities], updateResult:[$updateResult]")
      } yield updateResult
    }
  }

  implicit def StreamReadCache[T: Schema]: ZStreamCache[Any, Throwable, T] = new ZStreamCache[Any, Throwable, T] {
    override def getIfPresent(business: => ZStream[Any, Throwable, T])(identities: List[String], args: List[_]): ZStream[Any, Throwable, T] = {
      val key = cacheKey(identities)
      val field = cacheField(args)
      for {
        cacheValue <- ZStream.fromEffect(ZRedisService.hGet[T](key, field))
        _ <- if (ZRedisConfiguration.disabledLog) ZStream.unit else LogUtils.debugS(s"Redis ZStream getIfPresent: identity:[$key],field:[$field],cacheValue:[$cacheValue]")
        result <- cacheValue.fold(business.mapM(r => ZRedisService.hSet[T](key, field, r).as(r)))(value => ZStream.fromEffect(ZIO.effectTotal(value)))
        _ <- if (ZRedisConfiguration.disabledLog) ZStream.unit else LogUtils.debugS(s"Redis ZStream getIfPresent: identity:[$key],field:[$field],result:[$result]")
      } yield result
    }
  }

  implicit def UpdateCache[T: Schema]: ZIOUpdateCache[Any, Throwable, T] = new ZIOUpdateCache[Any, Throwable, T] {
    override def evict(business: => ZIO[Any, Throwable, T])(identities: List[String]): ZIO[Any, Throwable, T] = {
      for {
        updateResult <- ZIO.foreach_(identities)(key => ZRedisService.del(key)) *> business
        _ <- LogUtils.debug(s"Redis ZIO update: identities:[$identities], updateResult:[$updateResult]").unless(ZRedisConfiguration.disabledLog)
      } yield updateResult
    }
  }

  implicit def ReadCache[T: Schema]: ZIOCache[Any, Throwable, T] = new ZIOCache[Any, Throwable, T] {
    override def getIfPresent(business: => ZIO[Any, Throwable, T])(identities: List[String], args: List[_]): ZIO[Any, Throwable, T] = {
      val key = cacheKey(identities)
      val field = cacheField(args)
      for {
        cacheValue <- ZRedisService.hGet[T](key, field)
        _ <- LogUtils.debug(s"Redis ZIO getIfPresent: identity:[$key], field:[$field], cacheValue:[$cacheValue]").unless(ZRedisConfiguration.disabledLog)
        result <- cacheValue.fold(business.tap(r => ZRedisService.hSet[T](key, field, r).as(r)))(value => ZIO.effectTotal(value))
        _ <- LogUtils.debug(s"Redis ZIO getIfPresent: identity:[$key], field:[$field], result:[$result]").unless(ZRedisConfiguration.disabledLog)
      } yield result
    }
  }
}
