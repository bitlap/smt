package org.bitlap.common
import scala.collection.mutable

/** @author
 *    梦境迷离
 *  @version 1.0,2022/5/1
 */
object MacroCache {

  private var builderCount  = 0
  private var identityCount = 0

  def getBuilderId: Int = builderCount.synchronized {
    builderCount += 1; builderCount
  }

  def getIdentityId: Int = identityCount.synchronized {
    identityCount += 1; identityCount
  }

  lazy val builderFunctionTrees: mutable.Map[Int, mutable.Map[String, Any]] = mutable.Map.empty

  lazy val classFieldNameMapping: mutable.Map[Int, mutable.Map[String, String]] = mutable.Map.empty

  lazy val classFieldTypeMapping: mutable.Map[Int, mutable.Map[String, Any]] = mutable.Map.empty
}
