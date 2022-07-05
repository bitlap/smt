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

import org.bitlap.cache.GenericCache.Aux
import org.bitlap.common.{ CaseClassExtractor, CaseClassField }

import java.util.concurrent.atomic.AtomicBoolean
import scala.concurrent.{ ExecutionContext, Future }

/** @author
 *    梦境迷离
 *  @version 1.0,6/8/22
 */
object Cache {

  def getAsyncCache[K, T <: Product](implicit
    cache: Aux[K, T, Future],
    executionContext: ExecutionContext,
    keyBuilder: CacheKeyBuilder[K]
  ): CacheRef[K, T, Future] =
    new CacheRef[K, cache.Out, Future] {
      private lazy val initFlag = new AtomicBoolean(false)

      override def init(initKvs: => Map[K, cache.Out]): Future[Unit] =
        if (initFlag.compareAndSet(false, true)) {
          putTAll(initKvs)
        } else Future.successful(())

      override def putTAll(map: => Map[K, cache.Out]): Future[Unit] =
        cache.putAll(map)

      override def getT(key: K): Future[Option[cache.Out]] =
        cache.get(key)

      override def putT(key: K, value: cache.Out): Future[Unit] =
        cache.put(key, value)

      override def getTField(key: K, field: CaseClassField): Future[Option[field.Field]] =
        getT(key).map(opt =>
          opt.flatMap(t => CaseClassExtractor.ofValue[cache.Out](t, field).asInstanceOf[Option[field.Field]])
        )

      override def clear(): Future[Unit] = cache.clear()

      override def getAllT: Future[Map[K, T]] = cache.getAll
    }

  def getSyncCache[K, T <: Product](implicit
    cache: Aux[K, T, Identity],
    keyBuilder: CacheKeyBuilder[K]
  ): CacheRef[K, T, Identity] =
    new CacheRef[K, cache.Out, Identity] {
      private lazy val initFlag = new AtomicBoolean(false)

      override def init(initKvs: => Map[K, cache.Out]): Identity[Unit] =
        if (initFlag.compareAndSet(false, true)) {
          putTAll(initKvs)
        } else ()

      override def putTAll(map: => Map[K, cache.Out]): Identity[Unit] =
        map.foreach(kv => cache.put(kv._1, kv._2))

      override def getT(key: K): Identity[Option[cache.Out]] =
        cache.get(key)

      override def putT(key: K, value: cache.Out): Identity[Unit] =
        cache.put(key, value)

      override def getTField(key: K, field: CaseClassField): Identity[Option[field.Field]] =
        getT(key).flatMap(t => CaseClassExtractor.ofValue[cache.Out](t, field).asInstanceOf[Option[field.Field]])

      override def clear(): Identity[Unit] = cache.clear()

      override def getAllT: Identity[Map[K, cache.Out]] = cache.getAll
    }

}
