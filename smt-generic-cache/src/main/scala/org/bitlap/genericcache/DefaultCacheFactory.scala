package org.bitlap.genericcache

import scala.reflect.runtime.{ universe => ru }
import scala.reflect.runtime.universe._
import scala.reflect.ClassTag
import java.util.concurrent.atomic.AtomicBoolean

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

      override def getTField(key: String, field: CacheField)(implicit
        keyBuilder: CacheKeyBuilder[String]
      ): Option[field.Field] =
        getT(key).flatMap(t => getCaseClassFieldValue[T](t, field))

      override def init(initKvs: => Map[String, T]): Unit =
        if (initFlag.compareAndSet(false, true)) {
          putTAll(initKvs)
        }
    }

  private def getMethods[T: ru.TypeTag]: List[ru.MethodSymbol] = typeOf[T].members.collect {
    case m: MethodSymbol if m.isCaseAccessor => m
  }.toList

  // FIXME use macro to verify at compile time
  private def getCaseClassFieldValue[T: ru.TypeTag](obj: T, field: CacheField)(implicit
    classTag: ClassTag[T]
  ): Option[field.Field] = {
    val mirror = ru.runtimeMirror(getClass.getClassLoader)
    getMethods[T]
      .filter(_.name.toTermName.decodedName.toString == field.stringify)
      .map(m => mirror.reflect(obj).reflectField(m).get)
      .headOption
      .map(_.asInstanceOf[field.Field])
  }
}
