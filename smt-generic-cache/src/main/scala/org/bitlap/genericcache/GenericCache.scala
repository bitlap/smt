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

  def apply[K, Out0](maxSize: Int): Aux[K, Out0] = new GenericCache[K] {

    private val lruCache = new java.util.LinkedHashMap[String, Out0](maxSize, 0.75f, true)

    override type Out = Out0
    override def get(key: K)(implicit keyBuilder: CacheKeyBuilder[K]): Option[Out] = {
      val v = lruCache.get(keyBuilder.generateKey(key))
      if (v == null) None else Option(v)
    }

    private[genericcache] override def put(key: K, value: Out)(implicit keyBuilder: CacheKeyBuilder[K]): Unit =
      lruCache.put(keyBuilder.generateKey(key), value)
  }
}
