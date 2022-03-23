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

package org.bitlap.cacheable.caffeine

import com.github.benmanes.caffeine.cache.{ Cache, Caffeine }
import com.typesafe.config.{ Config, ConfigFactory }

import java.util.concurrent.{ ConcurrentHashMap, TimeUnit }
import zio.stm.{ TRef, ZSTM }
import zio.stm.USTM

/**
 *
 * @author 梦境迷离
 * @version 1.0,2022/3/21
 */
object ZCaffeine {

  private val conf: Config = ConfigFactory.load("reference.conf")
  private val custom: Config = ConfigFactory.load("application.conf").withFallback(conf)

  private[caffeine] lazy val disabledLog: Boolean = custom.getBoolean("caffeine.disabledLog")

  private lazy val maximumSize = custom.getInt("caffeine.maximumSize")
  private lazy val expireAfterWriteSeconds = custom.getInt("caffeine.expireAfterWriteSeconds")

  val hashCache: Cache[String, ConcurrentHashMap[String, Any]] = Caffeine.newBuilder()
    .maximumSize(maximumSize)
    .expireAfterWrite(expireAfterWriteSeconds, TimeUnit.SECONDS)
    .build[String, ConcurrentHashMap[String, Any]]

  private val cacheRef: USTM[TRef[Cache[String, ConcurrentHashMap[String, Any]]]] = TRef.make(hashCache)

  def hGet[T](key: String, field: String): ZSTM[Any, Throwable, Option[T]] = {
    for {
      cache <- cacheRef
      chm <- cache.get
    } yield {
      val hashMap = chm.getIfPresent(key)
      if (hashMap == null || hashMap.isEmpty) {
        None
      } else {
        val fieldValue = hashMap.get(field)
        if (fieldValue == null || !fieldValue.isInstanceOf[T]) {
          None
        } else {
          Some(fieldValue.asInstanceOf[T])
        }
      }
    }
  }

  def del(key: String): ZSTM[Any, Throwable, Unit] = {
    for {
      cache <- cacheRef
      ret <- cache.update { stmCache =>
        stmCache.invalidate(key)
        stmCache
      }
    } yield ret
  }

  def hSet[T](key: String, field: String, value: Any): ZSTM[Any, Throwable, T] = {
    for {
      cache <- cacheRef
      c <- cache.modify { stmCache =>
        val hashMap = stmCache.getIfPresent(key)
        if (hashMap == null || hashMap.isEmpty) {
          val chm = new ConcurrentHashMap[String, Any]()
          chm.put(field, value)
          stmCache.put(key, chm)
        } else {
          hashMap.put(field, value)
          stmCache.put(key, new ConcurrentHashMap(hashMap))
        }
        stmCache -> stmCache
      }
    } yield c.getIfPresent(key).get(field).asInstanceOf[T]

  }
}
