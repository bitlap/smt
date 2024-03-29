/*
 * Copyright (c) 2023 bitlap
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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

  lazy val classFieldDefaultValueMapping: mutable.Map[Int, mutable.Map[String, Any]] = mutable.Map.empty

  lazy val transformerOptionsMapping: mutable.Map[Int, mutable.Set[Options]] = mutable.Map.empty
}
