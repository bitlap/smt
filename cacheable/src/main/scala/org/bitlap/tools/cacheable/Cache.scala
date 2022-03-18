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

import zio.stream.ZStream
import zio.ZIO
import zio.schema.Schema

/**
 * A distributed cache for zio.
 *
 * @tparam Z The result type of the function that returns the ZIO or ZStream effect.
 */
trait Cache[Z] {

  /**
   * Cache when reading
   *
   * @param business The function that returns the ZIO or ZStream effect.
   * @param identity The cache key for storing.
   * @param args     The parameters fo the business function.
   * @return The result fo the business function.
   */
  def cacheRead(business: => Z)(identity: String, args: List[_]): Z

}

object Cache {

  def apply[R, E, T](business: => ZStream[R, E, T])(identity: String, args: List[_])(implicit cacheRead: ZStreamCache[R, E, T]): ZStream[R, E, T] = {
    cacheRead.cacheRead(business)(identity, args)
  }

  def apply[R, E, T](business: => ZIO[R, E, T])(identity: String, args: List[_])(implicit cacheRead: ZIOCache[R, E, T]): ZIO[R, E, T] = {
    cacheRead.cacheRead(business)(identity, args)
  }

  implicit def StreamCacheRead[T: Schema]: ZStreamCache[Any, Throwable, T] = new ZStreamCache[Any, Throwable, T] {
    override def cacheRead(business: => ZStream[Any, Throwable, T])(identity: String, args: List[_]): ZStream[Any, Throwable, T] = {
      val $key = s"$identity"
      val $field = args.map(_.toString).mkString("-")
      val $default = business.mapM(r => ZioRedisService.hSet[T]($key, $field, r).as(r))
      for {
        cacheValue <- ZStream.fromEffect(ZioRedisService.hGet[T]($key, $field))
        _ <- LogUtils.debugS(s"identity:[${$key}],field:[${$field}],cacheValue:[$cacheValue]")
        result <- cacheValue.fold($default)(value => ZStream.succeed(value))
        _ <- LogUtils.debugS(s"identity:[${$key}],field:[${$field}],result:[$result]")
      } yield result
    }
  }

  implicit def CacheRead[T: Schema]: ZIOCache[Any, Throwable, T] = new ZIOCache[Any, Throwable, T] {
    override def cacheRead(business: => ZIO[Any, Throwable, T])(identity: String, args: List[_]): ZIO[Any, Throwable, T] = {
      val $key = s"$identity"
      val $field = args.map(_.toString).mkString("-")
      val $default = business.tap(r => ZioRedisService.hSet[T]($key, $field, r).as(r))
      for {
        cacheValue <- ZioRedisService.hGet[T]($key, $field)
        _ <- LogUtils.debug(s"identity:[${$key}], field:[${$field}], cacheValue:[$cacheValue]")
        result <- cacheValue.fold($default)(value => ZIO.succeed(value))
        _ <- LogUtils.debug(s"identity:[${$key}], field:[${$field}], result:[$result]")
      } yield result
    }
  }
}
