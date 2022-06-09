package org.bitlap.genericcache

import java.util.UUID
import java.time.LocalDateTime
import java.time.ZonedDateTime

/** @author
 *    梦境迷离
 *  @version 1.0,6/8/22
 */
trait CacheKeyBuilder[T] {
  def generateKey(key: T): String
}

object CacheKeyBuilder {

  implicit val intKey: CacheKeyBuilder[Int] = new CacheKeyBuilder[Int] {
    override def generateKey(key: Int): String = key.toString
  }

  implicit val stringKey: CacheKeyBuilder[String] = new CacheKeyBuilder[String] {
    override def generateKey(key: String): String = key
  }

  implicit val longKey: CacheKeyBuilder[Long] = new CacheKeyBuilder[Long] {
    override def generateKey(key: Long): String = key.toString
  }

  implicit val doubleKey: CacheKeyBuilder[Double] = new CacheKeyBuilder[Double] {
    override def generateKey(key: Double): String = String.valueOf(key)
  }

  implicit val uuidKey: CacheKeyBuilder[UUID] = new CacheKeyBuilder[UUID] {
    override def generateKey(key: UUID): String = key.toString
  }

  implicit val localDateTimeKey: CacheKeyBuilder[LocalDateTime] = new CacheKeyBuilder[LocalDateTime] {
    override def generateKey(key: LocalDateTime): String = key.toString
  }

  implicit val zoneDateTimeKey: CacheKeyBuilder[ZonedDateTime] = new CacheKeyBuilder[ZonedDateTime] {
    override def generateKey(key: ZonedDateTime): String = key.toString
  }
}
