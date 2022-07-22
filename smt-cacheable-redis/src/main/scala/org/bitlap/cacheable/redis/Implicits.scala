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

import org.bitlap.cacheable.core.{ Utils, ZIOCache, ZIOUpdateCache, ZStreamCache, ZStreamUpdateCache }
import zio.ZIO
import zio.schema.Schema
import zio.stream.ZStream
import zio.Chunk
import java.util.concurrent.atomic.AtomicLong
import org.bitlap.cacheable.core.UZStream
import zio.Task

/** redis cache
 *
 *  @author
 *    梦境迷离
 *  @version 1.0,2022/3/21
 */
object Implicits {

  implicit def StreamUpdateCache[T: Schema]: ZStreamUpdateCache[T] =
    new ZStreamUpdateCache[T] {
      override def evict(business: => UZStream[T])(identities: List[String]): UZStream[T] =
        for {
          updateResult <- ZStream
            .fromIterable(identities)
            .flatMap(key => ZStream.fromEffect(ZRedisService.del(key))) *> business
          _ <- Utils
            .debugS(s"Redis ZStream update >>> identities:[$identities], updateResult:[$updateResult]")
            .when(!ZRedisConfiguration.disabledLog)
        } yield updateResult
    }

  implicit def StreamReadCache[T: Schema]: ZStreamCache[T] = new ZStreamCache[T] {
    override def getIfPresent(
      business: => UZStream[T]
    )(identities: List[String], args: List[_]): ZStream[Any, Throwable, T] = {
      val key            = cacheKey(identities)
      val field          = cacheField(args)
      lazy val ret       = business.runCollect.tap(r => ZRedisService.hSet[Chunk[T]](key, field, r))
      lazy val resultFun = (chunk: Chunk[T]) => if (chunk.isEmpty) ret else ZIO.succeed(chunk)
      lazy val count     = new AtomicLong(0L)
      for {
        // TODO fix it, cannot get case class from redis and not lock
        cacheValue <- ZStream.fromEffect(ZRedisService.hGet[Chunk[T]](key, field)).map(_.getOrElse(Chunk.empty))
        _ <- Utils
          .debugS(s"Redis ZStream getIfPresent >>> identity:[$key],field:[$field],cacheValue:[$cacheValue]")
          .when(!ZRedisConfiguration.disabledLog)
        ret    <- ZStream.fromEffect(resultFun(cacheValue))
        result <- ZStream.fromIterable(ret)
        _ <-
          Utils
            .debugS(
              s"Redis ZStream getIfPresent >>> identity:[$key],field(${count.incrementAndGet()}):[$field],result:[$result]"
            )
            .when(!ZRedisConfiguration.disabledLog)
      } yield result
    }
  }

  implicit def UpdateCache[T: Schema]: ZIOUpdateCache[T] = new ZIOUpdateCache[T] {
    override def evict(business: => Task[T])(identities: List[String]): Task[T] =
      for {
        updateResult <- ZIO.foreach_(identities)(key => ZRedisService.del(key)) *> business
        _ <- Utils
          .debug(s"Redis ZIO update >>> identities:[$identities], updateResult:[$updateResult]")
          .when(!ZRedisConfiguration.disabledLog)
      } yield updateResult
  }

  implicit def ReadCache[T: Schema]: ZIOCache[T] = new ZIOCache[T] {
    override def getIfPresent(
      business: => Task[T]
    )(identities: List[String], args: List[_]): Task[T] = {
      val key   = cacheKey(identities)
      val field = cacheField(args)
      for {
        cacheValue <- ZRedisService.hGet[T](key, field)
        _ <- Utils
          .debug(s"Redis ZIO getIfPresent: identity:[$key], field:[$field], cacheValue:[$cacheValue]")
          .when(!ZRedisConfiguration.disabledLog)
        result <- cacheValue.fold(business.tap(r => ZRedisService.hSet[T](key, field, r).as(r)))(value =>
          ZIO.effectTotal(value)
        )
        _ <- Utils
          .debug(s"Redis ZIO getIfPresent >>> identity:[$key], field:[$field], result:[$result]")
          .when(!ZRedisConfiguration.disabledLog)
      } yield result
    }
  }
}
