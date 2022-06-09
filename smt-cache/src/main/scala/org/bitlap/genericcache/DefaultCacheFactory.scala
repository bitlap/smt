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

package org.bitlap.genericcache

import java.util.concurrent.atomic.AtomicBoolean
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._
import org.bitlap.common.{ CaseClassExtractor, CaseClassField }

/** @author
 *    梦境迷离
 *  @version 1.0,6/8/22
 */
object DefaultCacheFactory {

  def createCache[T <: Product](cacheType: CacheType)(implicit
    classTag: ClassTag[T],
    typeTag: TypeTag[T]
  ): CacheRef[String, T] =
    new CacheRef[String, T] {
      private lazy val Cache: GenericCache.Aux[String, T] = GenericCache[String, T](cacheType)
      private lazy val initFlag                           = new AtomicBoolean(false)

      override def putTAll(map: => Map[String, T]): Unit =
        map.foreach(kv => Cache.put(kv._1, kv._2))

      override def getT(key: String)(implicit keyBuilder: CacheKeyBuilder[String]): Option[T] =
        Cache.get(key)

      override def putT(key: String, value: T)(implicit keyBuilder: CacheKeyBuilder[String]): Unit =
        Cache.put(key, value)

      override def getTField(key: String, field: CaseClassField)(implicit
        keyBuilder: CacheKeyBuilder[String]
      ): Option[field.Field] =
        getT(key).flatMap(t => CaseClassExtractor.getFieldValueUnSafely[T](t, field))

      override def init(initKvs: => Map[String, T]): Unit =
        if (initFlag.compareAndSet(false, true)) {
          putTAll(initKvs)
        }
        
    }
    
}
