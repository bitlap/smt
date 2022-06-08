package org.bitlap.genericcache

/** @author
 *    梦境迷离
 *  @version 1.0,6/8/22
 */
trait CacheType

object CacheType {

  case class Lru(maxSize: Int = 1000) extends CacheType
  case object Normal                  extends CacheType
}
