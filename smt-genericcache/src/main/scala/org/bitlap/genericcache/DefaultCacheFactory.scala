package org.bitlap.genericcache

import java.util.concurrent.atomic.AtomicBoolean
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._
import org.bitlap.common.{ CaseClassExtractor, CaseClassField }

/** @author
 *    梦境迷离
 *  @version 1.0,6/8/22
 */
object DefaultCacheFactory {

  def createCache[T <: Product](cacheType: CacheType)(implicit
    classTag: ClassTag[T],
    typeTag: TypeTag[T]
  ): CacheRef[String, T] =
    new CacheRef[String, T] {
      private lazy val Cache: GenericCache.Aux[String, T] = GenericCache[String, T](cacheType)
      private lazy val initFlag                           = new AtomicBoolean(false)

      override def putTAll(map: => Map[String, T]): Unit =
        map.foreach(kv => Cache.put(kv._1, kv._2))

      override def getT(key: String)(implicit keyBuilder: CacheKeyBuilder[String]): Option[T] =
        Cache.get(key)

      override def putT(key: String, value: T)(implicit keyBuilder: CacheKeyBuilder[String]): Unit =
        Cache.put(key, value)

      override def getTField(key: String, field: CaseClassField)(implicit
        keyBuilder: CacheKeyBuilder[String]
      ): Option[field.Field] =
        getT(key).flatMap(t => CaseClassExtractor.getFieldValueUnSafely[T](t, field))

      override def init(initKvs: => Map[String, T]): Unit =
        if (initFlag.compareAndSet(false, true)) {
          putTAll(initKvs)
        }
        
    }
    
}
