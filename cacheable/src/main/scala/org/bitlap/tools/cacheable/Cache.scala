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

import zio.ZIO
import zio.schema.Schema
import zio.stream.ZStream

/**
 * A distributed cache for zio.
 *
 * @tparam Z The result type of the function that returns the ZIO or ZStream effect.
 */
trait Cache[Z] {

  /**
   * Get cache or getAndSet cache from Cache while read data.
   *
   * @param business The function that returns the ZIO or ZStream effect.
   * @param identity The cache key for storing.
   * @param args     The parameters fo the business function.
   * @return The result fo the business function.
   */
  def getIfPresent(business: => Z)(identity: String, args: List[_]): Z

  /**
   * Update cache or add into cache while data update.
   *
   * @param business The function that returns the ZIO or ZStream effect.
   * @param identity The cache key for storing.
   * @param args     The parameters fo the business function.
   * @return The result fo the business function.
   */
  def update(business: => Z)(identity: String, args: List[_]): Z

  /**
   * Build a string for cache key.
   *
   * @param key hash key
   * @return string
   */
  def cacheKey(key: String): String = key

  /**
   * Build a string for cache field.
   *
   * @param args The parameters fo the business function.
   * @return hash field
   */
  def cacheField(args: List[_]): String = args.map(_.toString).mkString("-")

}

object Cache {

  def apply[R, E, T](business: => ZStream[R, E, T])(identity: String, args: List[_])(implicit streamCache: ZStreamCache[R, E, T]): ZStream[R, E, T] = {
    streamCache.getIfPresent(business)(identity, args)
  }

  def apply[R, E, T](business: => ZIO[R, E, T])(identity: String, args: List[_])(implicit cache: ZIOCache[R, E, T]): ZIO[R, E, T] = {
    cache.getIfPresent(business)(identity, args)
  }

  def of[R, E, T](business: => ZStream[R, E, T])(identity: String, args: List[_])(implicit streamCache: ZStreamUpdateCache[R, E, T]): ZStream[R, E, T] = {
    streamCache.update(business)(identity, args)
  }

  def of[R, E, T](business: => ZIO[R, E, T])(identity: String, args: List[_])(implicit cache: ZIOUpdateCache[R, E, T]): ZIO[R, E, T] = {
    cache.update(business)(identity, args)
  }

  implicit def StreamUpdateCache[T: Schema]: ZStreamUpdateCache[Any, Throwable, T] = new ZStreamUpdateCache[Any, Throwable, T] {
    override def update(business: => ZStream[Any, Throwable, T])(identity: String, args: List[_]): ZStream[Any, Throwable, T] = {
      val $key = cacheKey(identity)
      val $field = cacheField(args)
      for {
        updateResult <- ZStream.fromEffect(ZRedisService.hDel($key, $field)) *> business
        _ <- LogUtils.debugS(s"update: identity:[${$key}], field:[${$field}], updateResult:[$updateResult]")
        result <- if (updateResult != null) {
          ZStream.fromEffect(ZRedisService.hSet[T]($key, $field, updateResult).as(updateResult))
        } else {
          ZStream.fromEffect(ZIO.effect(null.asInstanceOf[T])) // TODO
        }
        _ <- LogUtils.debugS(s"update: identity:[${$key}], field:[${$field}], result:[$result]")
      } yield result
    }
  }

  implicit def StreamReadCache[T: Schema]: ZStreamCache[Any, Throwable, T] = new ZStreamCache[Any, Throwable, T] {
    override def getIfPresent(business: => ZStream[Any, Throwable, T])(identity: String, args: List[_]): ZStream[Any, Throwable, T] = {
      val $key = cacheKey(identity)
      val $field = cacheField(args)
      val $default = business.mapM(r => ZRedisService.hSet[T]($key, $field, r).as(r))
      for {
        cacheValue <- ZStream.fromEffect(ZRedisService.hGet[T]($key, $field))
        _ <- LogUtils.debugS(s"getIfPresent: identity:[${$key}],field:[${$field}],cacheValue:[$cacheValue]")
        result <- cacheValue.fold($default)(value => ZStream.succeed(value))
        _ <- LogUtils.debugS(s"getIfPresent: identity:[${$key}],field:[${$field}],result:[$result]")
      } yield result
    }
  }

  implicit def UpdateCache[T: Schema]: ZIOUpdateCache[Any, Throwable, T] = new ZIOUpdateCache[Any, Throwable, T] {
    override def update(business: => ZIO[Any, Throwable, T])(identity: String, args: List[_]): ZIO[Any, Throwable, T] = {
      val $key = cacheKey(identity)
      val $field = cacheField(args)
      for {
        updateResult <- ZRedisService.hDel($key, $field) *> business
        _ <- LogUtils.debug(s"update: identity:[${$key}], field:[${$field}], updateResult:[$updateResult]")
        result <- if (updateResult != null) {
          ZRedisService.hSet[T]($key, $field, updateResult).as(updateResult)
        } else {
          ZIO.effect(null.asInstanceOf[T]) // TODO
        }
        _ <- LogUtils.debug(s"update: identity:[${$key}], field:[${$field}], result:[$result]")
      } yield result
    }
  }

  implicit def ReadCache[T: Schema]: ZIOCache[Any, Throwable, T] = new ZIOCache[Any, Throwable, T] {
    override def getIfPresent(business: => ZIO[Any, Throwable, T])(identity: String, args: List[_]): ZIO[Any, Throwable, T] = {
      val $key = cacheKey(identity)
      val $field = cacheField(args)
      val $default = business.tap(r => ZRedisService.hSet[T]($key, $field, r).as(r))
      for {
        cacheValue <- ZRedisService.hGet[T]($key, $field)
        _ <- LogUtils.debug(s"getIfPresent: identity:[${$key}], field:[${$field}], cacheValue:[$cacheValue]")
        result <- cacheValue.fold($default)(value => ZIO.succeed(value))
        _ <- LogUtils.debug(s"getIfPresent: identity:[${$key}], field:[${$field}], result:[$result]")
      } yield result
    }
  }
}
