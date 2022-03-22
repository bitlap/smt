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

package org.bitlap.cacheable.caffeine

import org.bitlap.cacheable.core._
import zio.ZIO
import zio.stream.ZStream

/**
 * redis cache
 *
 * @author 梦境迷离
 * @version 1.0,2022/3/21
 */
object Implicits {

  implicit def StreamUpdateCache[T]: ZStreamUpdateCache[Any, Throwable, T] = new ZStreamUpdateCache[Any, Throwable, T] {
    override def evict(business: => ZStream[Any, Throwable, T])(identities: List[String]): ZStream[Any, Throwable, T] = {
      for {
        updateResult <- ZStream.fromIterable(identities).map(key => ZCaffeine.del(key)) *> business
        _ <- if (ZCaffeine.disabledLog) ZStream.unit else LogUtils.debugS(s"Caffeine ZStream update: identities:[$identities], updateResult:[$updateResult]")
      } yield updateResult
    }
  }

  implicit def StreamReadCache[T]: ZStreamCache[Any, Throwable, T] = new ZStreamCache[Any, Throwable, T] {
    override def getIfPresent(business: => ZStream[Any, Throwable, T])(identities: List[String], args: List[_]): ZStream[Any, Throwable, T] = {
      val key = cacheKey(identities)
      val field = cacheField(args)
      for {
        cacheValue <- ZStream.fromEffect(ZCaffeine.hGet[T](key, field))
        _ <- if (ZCaffeine.disabledLog) ZStream.unit else LogUtils.debugS(s"Caffeine ZStream getIfPresent: identity:[$key],field:[$field],cacheValue:[$cacheValue]")
        result <- cacheValue.fold(business.mapM(r => ZCaffeine.hSet(key, field, r).as(r)))(value => ZStream.fromEffect(ZIO.effectTotal(value)))
        _ <- if (ZCaffeine.disabledLog) ZStream.unit else LogUtils.debugS(s"Caffeine ZStream getIfPresent: identity:[$key],field:[$field],result:[$result]")
      } yield result
    }
  }

  implicit def UpdateCache[T]: ZIOUpdateCache[Any, Throwable, T] = new ZIOUpdateCache[Any, Throwable, T] {
    override def evict(business: => ZIO[Any, Throwable, T])(identities: List[String]): ZIO[Any, Throwable, T] = {
      for {
        updateResult <- ZIO.foreach_(identities)(key => ZCaffeine.del(key)) *> business
        _ <- LogUtils.debug(s"Caffeine ZIO update: identities:[$identities], updateResult:[$updateResult]").unless(ZCaffeine.disabledLog)
      } yield updateResult
    }
  }

  implicit def ReadCache[T]: ZIOCache[Any, Throwable, T] = new ZIOCache[Any, Throwable, T] {
    override def getIfPresent(business: => ZIO[Any, Throwable, T])(identities: List[String], args: List[_]): ZIO[Any, Throwable, T] = {
      val key = cacheKey(identities)
      val field = cacheField(args)
      for {
        cacheValue <- ZCaffeine.hGet[T](key, field)
        _ <- LogUtils.debug(s"Caffeine ZIO getIfPresent: identity:[$key], field:[$field], cacheValue:[$cacheValue]").unless(ZCaffeine.disabledLog)
        result <- cacheValue.fold(business.tap(r => ZCaffeine.hSet(key, field, r).as(r)))(value => ZIO.effectTotal(value))
        _ <- LogUtils.debug(s"Caffeine ZIO getIfPresent: identity:[$key], field:[$field], result:[$result]").unless(ZCaffeine.disabledLog)
      } yield result
    }
  }
}
