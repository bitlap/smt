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

import zio.stm.STM
import zio.Chunk

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
      ZStream.fromEffect(STM.atomically {
        STM.foreach_(identities)(key => ZCaffeine.del(key))
      }) *> ({
        Utils.debugS(s"Caffeine ZStream update >>> identities:[$identities]")
          .when(!ZCaffeine.disabledLog)
      } *> business)
    }
  }

  implicit def StreamReadCache[T]: ZStreamCache[Any, Throwable, T] = new ZStreamCache[Any, Throwable, T] {
    override def getIfPresent(business: => ZStream[Any, Throwable, T])(identities: List[String], args: List[_]): ZStream[Any, Throwable, T] = {
      val key = cacheKey(identities)
      val field = cacheField(args)
      // TODO fix it?
      lazy val syncResult = zio.Runtime.default.unsafeRun(business.runCollect)
      val stmResult = STM.atomically {
        for {
          chunk <- ZCaffeine.hGet[Chunk[T]](key, field).map(_.getOrElse(Chunk.empty))
          ret <- if (chunk.isEmpty) ZCaffeine.hSet[Chunk[T]](key, field, syncResult) else STM.succeed(chunk)
        } yield ret
      }
      for {
        ret <- ZStream.fromEffect(stmResult)
        _ <- Utils.debugS(s"Caffeine ZStream getIfPresent >>> identity:[$key],field:[$field],result:[$ret]").when(!ZCaffeine.disabledLog)
        r <- ZStream.fromIterable(ret)
      } yield r
    }
  }

  implicit def UpdateCache[T]: ZIOUpdateCache[Any, Throwable, T] = new ZIOUpdateCache[Any, Throwable, T] {
    override def evict(business: => ZIO[Any, Throwable, T])(identities: List[String]): ZIO[Any, Throwable, T] = {
      STM.atomically {
        STM.foreach_(identities)(key => ZCaffeine.del(key))
      } *> {
        business.tap(updateResult => Utils.debug(s"Caffeine ZIO update >>> identities:[$identities],updateResult:[$updateResult]")
          .when(!ZCaffeine.disabledLog))
      }
    }
  }

  implicit def ReadCache[T]: ZIOCache[Any, Throwable, T] = new ZIOCache[Any, Throwable, T] {
    override def getIfPresent(business: => ZIO[Any, Throwable, T])(identities: List[String], args: List[_]): ZIO[Any, Throwable, T] = {
      val key = cacheKey(identities)
      val field = cacheField(args)
      lazy val syncResult = zio.Runtime.default.unsafeRun(business)
      // TODO fix it?
      STM.atomically {
        for {
          chunk <- ZCaffeine.hGet[T](key, field)
          ret <- chunk.fold(ZCaffeine.hSet[T](key, field, syncResult))(c => STM.succeed(c))
        } yield ret
      }.tap(ret => Utils.debug(s"Caffeine ZIO getIfPresent >>> identity:[$key],field:[$field],result:[$ret]")
        .when(!ZCaffeine.disabledLog))
    }
  }
}
