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

import java.util.UUID

/** @author
 *    梦境迷离
 *  @version 1.0,6/8/22
 */
trait CacheKeyBuilder[T] {
  def generateKey(key: T): String

  def unGenerateKey(key: String): T
}

object CacheKeyBuilder {

  implicit val intKey: CacheKeyBuilder[Int] = new CacheKeyBuilder[Int] {
    override def generateKey(key: Int): String = key.toString

    override def unGenerateKey(key: String): Int = key.toInt
  }

  implicit val stringKey: CacheKeyBuilder[String] = new CacheKeyBuilder[String] {
    override def generateKey(key: String): String = key

    override def unGenerateKey(key: String): String = key
  }

  implicit val longKey: CacheKeyBuilder[Long] = new CacheKeyBuilder[Long] {
    override def generateKey(key: Long): String = key.toString

    override def unGenerateKey(key: String): Long = key.toLong
  }

  implicit val doubleKey: CacheKeyBuilder[Double] = new CacheKeyBuilder[Double] {
    override def generateKey(key: Double): String = String.valueOf(key)

    override def unGenerateKey(key: String): Double = key.toDouble
  }

  implicit val uuidKey: CacheKeyBuilder[UUID] = new CacheKeyBuilder[UUID] {
    override def generateKey(key: UUID): String = key.toString

    override def unGenerateKey(key: String): UUID = UUID.fromString(key)
  }
}
