package org.bitlap.genericcache

/** @author
 *    梦境迷离
 *  @version 1.0,6/8/22
 */
trait CacheRef[In, T <: Product] {

  def init(initKvs: => Map[String, T]): Unit

  def putTAll(map: => Map[String, T]): Unit

  def getT(key: In)(implicit keyBuilder: CacheKeyBuilder[In]): Option[T]

  def putT(key: In, value: T)(implicit keyBuilder: CacheKeyBuilder[String]): Unit

  def getTField(key: In, field: CacheField)(implicit keyBuilder: CacheKeyBuilder[In]): Option[field.Field]
}
