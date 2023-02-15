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
trait CacheKeySerde[T] {
  def serialize(key: T): String

  def deserialize(key: String): T
}

object CacheKeySerde {

  implicit val intKey: CacheKeySerde[Int] = new CacheKeySerde[Int] {
    override def serialize(key: Int): String = key.toString

    override def deserialize(key: String): Int = key.toInt
  }

  implicit val stringKey: CacheKeySerde[String] = new CacheKeySerde[String] {
    override def serialize(key: String): String = key

    override def deserialize(key: String): String = key
  }

  implicit val longKey: CacheKeySerde[Long] = new CacheKeySerde[Long] {
    override def serialize(key: Long): String = key.toString

    override def deserialize(key: String): Long = key.toLong
  }

  implicit val doubleKey: CacheKeySerde[Double] = new CacheKeySerde[Double] {
    override def serialize(key: Double): String = String.valueOf(key)

    override def deserialize(key: String): Double = key.toDouble
  }

  implicit val uuidKey: CacheKeySerde[UUID] = new CacheKeySerde[UUID] {
    override def serialize(key: UUID): String = key.toString

    override def deserialize(key: String): UUID = UUID.fromString(key)
  }
}
