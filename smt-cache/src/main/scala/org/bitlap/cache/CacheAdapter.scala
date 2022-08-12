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
trait CacheAdapter[V] {

  def getAllKeys: Set[String]

  def batchPut(data: Map[String, V]): Unit

  def put(k: String, v: V): Unit

  def get(k: String): V

  def clear(): Unit

  def remove(k: String): Unit
}

object CacheAdapter {

  def adapted[V](cacheStrategy: CacheStrategy): CacheAdapter[V] =
    cacheStrategy match {
      case CacheStrategy.Lru(maxSize) =>
        new LruHashMapCacheAdapter(
          Collections.synchronizedMap(new java.util.LinkedHashMap[String, V](16, 0.75f, true) {
            override def removeEldestEntry(eldest: java.util.Map.Entry[String, V]): Boolean = size > maxSize
          })
        )
      case CacheStrategy.Normal =>
        new ConcurrentMapCacheAdapter(new java.util.concurrent.ConcurrentHashMap[String, V]())
      case CacheStrategy.CustomCacheStrategy(cacheAdapter) => cacheAdapter.asInstanceOf[CacheAdapter[V]]
    }

  class LruHashMapCacheAdapter[V](underlyingCache: java.util.Map[String, V]) extends CacheAdapter[V] {

    override def getAllKeys: Set[String] = underlyingCache.keySet().asScala.toSet

    override def batchPut(data: Map[String, V]): Unit = underlyingCache.putAll(data.asJava)

    override def put(k: String, v: V): Unit = underlyingCache.put(k, v)

    override def get(k: String): V = underlyingCache.get(k)

    override def clear(): Unit = underlyingCache.clear()

    override def remove(k: String): Unit = underlyingCache.remove(k)
  }

  class ConcurrentMapCacheAdapter[V](underlyingCache: java.util.concurrent.ConcurrentMap[String, V])
      extends CacheAdapter[V] {

    override def getAllKeys: Set[String] = underlyingCache.keySet().asScala.toSet

    override def batchPut(data: Map[String, V]): Unit = underlyingCache.putAll(data.asJava)

    override def put(k: String, v: V): Unit = underlyingCache.put(k, v)

    override def get(k: String): V = underlyingCache.get(k)

    override def clear(): Unit = underlyingCache.clear()

    override def remove(k: String): Unit = underlyingCache.remove(k)
  }

}
