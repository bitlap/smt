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

package org.bitlap.cacheable.core

import zio.ZIO
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
   * @param business   The function that returns the ZIO or ZStream effect.
   * @param identities Append all strings for cache key.
   * @param args       The parameters of the business function.
   * @return The result fo the business function.
   */
  def getIfPresent(business: => Z)(identities: List[String], args: List[_]): Z

  /**
   * Evict cache while data update.
   *
   * @param business   The function that returns the ZIO or ZStream effect.
   * @param identities Append all strings for cache key.
   * @return The result fo the business function.
   */
  def evict(business: => Z)(identities: List[String]): Z

  /**
   * Build a string for cache key.
   *
   * @param keys Append all strings for hash key
   * @return string
   */
  def cacheKey(keys: List[String]): String = keys.mkString("-")

  /**
   * Build a string for cache field.
   *
   * @param args The parameters of the business function.
   * @return hash field
   */
  def cacheField(args: List[_]): String = args.map(_.toString).mkString("-")

}

object Cache {

  def apply[R, E, T](business: => ZStream[R, E, T])(identities: List[String], args: List[_])(implicit streamCache: ZStreamCache[R, E, T]): ZStream[R, E, T] = {
    streamCache.getIfPresent(business)(identities, args)
  }

  def apply[R, E, T](business: => ZIO[R, E, T])(identities: List[String], args: List[_])(implicit cache: ZIOCache[R, E, T]): ZIO[R, E, T] = {
    cache.getIfPresent(business)(identities, args)
  }

  def evict[R, E, T](business: => ZStream[R, E, T])(identities: List[String])(implicit streamCache: ZStreamUpdateCache[R, E, T]): ZStream[R, E, T] = {
    streamCache.evict(business)(identities)
  }

  def evict[R, E, T](business: => ZIO[R, E, T])(identities: List[String])(implicit cache: ZIOUpdateCache[R, E, T]): ZIO[R, E, T] = {
    cache.evict(business)(identities)
  }

}
