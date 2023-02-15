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

import scala.concurrent.{ ExecutionContext, Future }

/** @author
 *    梦境迷离
 *  @version 1.0,6/8/22
 */
object Cache {

  def apply[K, T <: Product](implicit
    cache: Aux[K, T, Future],
    executionContext: ExecutionContext,
    keySerde: CacheKeySerde[K]
  ): CacheRef[K, T, Future] =
    new CacheRef[K, cache.Out, Future] {
      override def batchPutF(data: => Map[K, cache.Out]): Future[Unit] =
        cache.putAll(data)

      override def getF(key: K): Future[Option[cache.Out]] =
        cache.get(key)

      override def putF(key: K, value: cache.Out): Future[Unit] =
        cache.put(key, value)

      override def clearF(): Future[Unit] = cache.clear()

      override def removeF(key: K): Future[Unit] = cache.remove(key)

      override def getAllF: Future[Map[K, cache.Out]] = cache.getAll

      override def refreshF(allNewData: Map[K, cache.Out]): Future[Unit] =
        this.synchronized {
          this.getAllF.map { t =>
            val invalidData = t.keySet.filterNot(allNewData.keySet)
            this.batchPutF(allNewData).map(_ => invalidData.foreach(this.removeF))
          }
        }
    }

}
