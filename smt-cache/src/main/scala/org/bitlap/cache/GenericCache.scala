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

package org.bitlap.cache
import java.util
import scala.concurrent.{ ExecutionContext, Future }

/** @author
 *    梦境迷离
 *  @version 1.0,6/8/22
 */
sealed trait GenericCache[K, F[_]] {

  type Out <: Product

  def get(key: K)(implicit keyBuilder: CacheKeyBuilder[K]): F[Option[Out]]

  def put(key: K, value: Out)(implicit keyBuilder: CacheKeyBuilder[K]): F[Unit]

}
object GenericCache {

  type Aux[K, Out0, F[_]] = GenericCache[K, F] { type Out = Out0 }

  def apply[K, Out0 <: Product](cacheStrategy: CacheStrategy): Aux[K, Out0, Identity] = new GenericCache[K, Identity] {
    private val typedCache = getCacheByType[Out0](cacheStrategy)
    override type Out = Out0
    override def get(key: K)(implicit keyBuilder: CacheKeyBuilder[K]): Identity[Option[Out]] = {
      val v = typedCache.get(keyBuilder.generateKey(key))
      if (v == null) None else Option(v)
    }

    override def put(key: K, value: Out)(implicit keyBuilder: CacheKeyBuilder[K]): Identity[Unit] =
      typedCache.put(keyBuilder.generateKey(key), value)
  }

  def apply[K, Out0 <: Product](
    cacheStrategy: CacheStrategy,
    executionContext: ExecutionContext
  ): Aux[K, Out0, Future] = {
    implicit val ec: ExecutionContext = executionContext
    new GenericCache[K, Future] {
      private val typedCache = getCacheByType[Out0](cacheStrategy)
      override type Out = Out0

      override def get(key: K)(implicit keyBuilder: CacheKeyBuilder[K]): Future[Option[Out]] =
        Future {
          val v = typedCache.get(keyBuilder.generateKey(key))
          if (v == null) None else Option(v)
        }

      def put(key: K, value: Out)(implicit keyBuilder: CacheKeyBuilder[K]): Future[Unit] =
        Future {
          typedCache.put(keyBuilder.generateKey(key), value)
        }
    }
  }

  private def getCacheByType[Out0](cacheType: CacheStrategy): util.Map[String, Out0] =
    cacheType match {
      case CacheStrategy.Lru(maxSize) => new java.util.LinkedHashMap[String, Out0](maxSize, 0.75f, true)
      case CacheStrategy.Normal       => new java.util.concurrent.ConcurrentHashMap[String, Out0]()
      // TODO other cache
    }
}
