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

import scala.jdk.CollectionConverters._

import java.util.Collections

/** @author
 *    梦境迷离
 *  @version 1.0,2022/7/5
 */
trait CacheContainer[V] {
  def getAllKeys: Set[String]
  def putAll(map: Map[String, V]): Unit
  def put(k: String, v: V): Unit
  def get(k: String): V
  def clear(): Unit
}
object CacheContainer {

  def getCacheByStrategy[V](cacheType: CacheStrategy): CacheContainer[V] =
    cacheType match {
      case CacheStrategy.Lru(maxSize) =>
        new LruHashMapCacheContainer(
          Collections.synchronizedMap(new java.util.LinkedHashMap[String, V](maxSize, 0.75f, true))
        )
      case CacheStrategy.Normal =>
        new ConcurrentMapContainer(new java.util.concurrent.ConcurrentHashMap[String, V]())
      case CacheStrategy.CustomUnderlyingCache(cacheContainer) => cacheContainer.asInstanceOf[CacheContainer[V]]
    }

  class LruHashMapCacheContainer[V](underlyingCache: java.util.Map[String, V]) extends CacheContainer[V] {

    override def getAllKeys: Set[String] = underlyingCache.keySet().asScala.toSet

    override def putAll(map: Map[String, V]): Unit = underlyingCache.putAll(map.asJava)

    override def put(k: String, v: V): Unit = underlyingCache.put(k, v)

    override def get(k: String): V = underlyingCache.get(k)

    override def clear(): Unit = underlyingCache.clear()
  }

  class ConcurrentMapContainer[V](underlyingCache: java.util.concurrent.ConcurrentMap[String, V])
      extends CacheContainer[V] {

    override def getAllKeys: Set[String] = underlyingCache.keySet().asScala.toSet

    override def putAll(map: Map[String, V]): Unit = underlyingCache.putAll(map.asJava)

    override def put(k: String, v: V): Unit = underlyingCache.put(k, v)

    override def get(k: String): V = underlyingCache.get(k)

    override def clear(): Unit = underlyingCache.clear()
  }

}
