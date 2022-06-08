package org.bitlap.genericcache

/** @author
 *    梦境迷离
 *  @version 1.0,6/8/22
 */
sealed trait GenericCache[K] {

  type Out

  def get(key: K)(implicit keyBuilder: CacheKeyBuilder[K]): Option[Out]

  private[genericcache] def put(key: K, value: Out)(implicit keyBuilder: CacheKeyBuilder[K]): Unit

}

object GenericCache {

  type Aux[K, Out0] = GenericCache[K] { type Out = Out0 }

  def apply[K, Out0](cacheType: CacheType): Aux[K, Out0] = new GenericCache[K] {
    private val typedCache = cacheType match {
      case CacheType.Lru(maxSize) => new java.util.LinkedHashMap[String, Out0](maxSize, 0.75f, true)
      case CacheType.Normal       => new java.util.LinkedHashMap[String, Out0]()
    }

    override type Out = Out0
    override def get(key: K)(implicit keyBuilder: CacheKeyBuilder[K]): Option[Out] = {
      val v = typedCache.get(keyBuilder.generateKey(key))
      if (v == null) None else Option(v)
    }

    private[genericcache] override def put(key: K, value: Out)(implicit keyBuilder: CacheKeyBuilder[K]): Unit =
      typedCache.put(keyBuilder.generateKey(key), value)
  }
}
