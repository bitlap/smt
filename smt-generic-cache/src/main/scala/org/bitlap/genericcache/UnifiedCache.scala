package org.bitlap.genericcache

/** @author
 *    梦境迷离
 *  @version 1.0,6/8/22
 */
trait UnifiedCache[In, T <: Product] {

  private[genericcache] def refresh(initKvs: => Map[String, T]): Unit

  def getT(key: In)(implicit keyBuilder: CacheKeyBuilder[In]): Option[T]

  def getTField(key: In, field: CacheField)(implicit keyBuilder: CacheKeyBuilder[In]): Option[field.Field]
}
