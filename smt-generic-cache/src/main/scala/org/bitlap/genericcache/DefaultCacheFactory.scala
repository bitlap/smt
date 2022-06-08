package org.bitlap.genericcache

import scala.reflect.runtime.{ universe => ru }
import scala.reflect.runtime.universe._
import scala.reflect.ClassTag

/** @author
 *    梦境迷离
 *  @version 1.0,6/8/22
 */
object DefaultCacheFactory {

  def createAndInitCache[T <: Product](kvs: => Map[String, T], maxSize: Int = 1000)(implicit
    classTag: ClassTag[T],
    typeTag: TypeTag[T]
  ): CacheRef[String, T] = {
    val uc: CacheRef[String, T] = new CacheRef[String, T] {
      private lazy val Cache: GenericCache.Aux[String, T] = GenericCache[String, T](maxSize)
      private[genericcache] def refresh(initKvs: => Map[String, T]): Unit =
        initKvs.foreach(kv => Cache.put(kv._1, kv._2))

      override def getT(key: String)(implicit keyBuilder: CacheKeyBuilder[String]): Option[T] =
        Cache.get(keyBuilder.generateKey(key))

      override def getTField(key: String, field: CacheField)(implicit
        keyBuilder: CacheKeyBuilder[String]
      ): Option[field.Field] =
        getT(key).flatMap(t => getCaseClassFieldValue[T](t, field))
    }
    uc.refresh(kvs)
    uc
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
