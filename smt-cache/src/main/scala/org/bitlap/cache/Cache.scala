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
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

/** @author
 *    梦境迷离
 *  @version 1.0,6/8/22
 */
object Cache {

  def getCache[T <: Product](implicit
    cache: Aux[String, T],
    classTag: ClassTag[T],
    typeTag: TypeTag[T]
  ): CacheRef[String, T] =
    new CacheRef[String, cache.Out] {
      private lazy val initFlag = new AtomicBoolean(false)

      override def init(initKvs: => Map[String, cache.Out]): Unit =
        if (initFlag.compareAndSet(false, true)) {
          putTAll(initKvs)
        }

      override def putTAll(map: => Map[String, cache.Out]): Unit =
        map.foreach(kv => cache.put(kv._1, kv._2))

      override def getT(key: String)(implicit keyBuilder: CacheKeyBuilder[String]): Option[cache.Out] = cache.get(key)

      override def putT(key: String, value: cache.Out)(implicit keyBuilder: CacheKeyBuilder[String]): Unit =
        cache.put(key, value)

      override def getTField(key: String, field: CaseClassField)(implicit
        keyBuilder: CacheKeyBuilder[String]
      ): Option[field.Field] =
        getT(key).flatMap(t => CaseClassExtractor.getFieldValueUnSafely[cache.Out](t, field))
    }

}