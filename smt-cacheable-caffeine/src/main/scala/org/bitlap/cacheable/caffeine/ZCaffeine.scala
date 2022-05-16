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
import org.bitlap.cacheable.core.Utils

import java.util.concurrent.{ ConcurrentHashMap, TimeUnit }
import scala.concurrent.duration.Duration

/** @author
 *    梦境迷离
 *  @version 1.0,2022/3/21
 */
object ZCaffeine {

  import zio.Task

  private val conf: Config   = ConfigFactory.load("reference.conf")
  private val custom: Config = ConfigFactory.load("application.conf").withFallback(conf)

  private[caffeine] lazy val disabledLog: Boolean = custom.getBoolean("caffeine.disabledLog")
  private[caffeine] lazy val calculateResultTimeout: Duration = Duration(
    custom.getString("caffeine.calculateResultTimeout")
  )

  private lazy val maximumSize: Int             = custom.getInt("caffeine.maximumSize")
  private lazy val expireAfterWriteSeconds: Int = custom.getInt("caffeine.expireAfterWriteSeconds")

  val hashCache: Cache[String, ConcurrentHashMap[String, Any]] = Caffeine
    .newBuilder()
    .maximumSize(maximumSize)
    .expireAfterWrite(expireAfterWriteSeconds, TimeUnit.SECONDS)
    .build[String, ConcurrentHashMap[String, Any]]

  def hGet[T](key: String, field: String): Task[Option[T]] =
    Utils.effectBlocking {
      key.synchronized {
        val hashMap = hashCache.getIfPresent(key)
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

  def hDel(key: String, field: String): Task[Unit] =
    Utils.effectBlocking {
      key.synchronized {
        val hashMap = hashCache.getIfPresent(key)
        if (hashMap == null || hashMap.isEmpty) {
          ()
        } else {
          hashMap.remove(field)
          hashCache.put(key, new ConcurrentHashMap(hashMap))
        }
      }
    }

  def del(key: String): Task[Unit] =
    Utils.effectBlocking {
      key.synchronized {
        hashCache.invalidate(key)
      }
    }

  def hSet(key: String, field: String, value: Any): Task[Unit] =
    Utils.effectBlocking {
      key.synchronized {
        val hashMap = hashCache.getIfPresent(key)
        if (hashMap == null || hashMap.isEmpty) {
          val chm = new ConcurrentHashMap[String, Any]()
          chm.put(field, value)
          hashCache.put(key, chm)
        } else {
          hashMap.put(field, value)
          hashCache.put(key, new ConcurrentHashMap(hashMap))
        }
      }
    }
}
