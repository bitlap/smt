/*
 * Copyright (c) 2022 bitlap
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

package org.bitlap.tools.logs

import org.bitlap.tools.logs.extension._
import org.bitlap.tools.logs.impl._

/** @author
 *    梦境迷离
 *  @version 1.0,2022/3/29
 */
object LogType {

  val JLog               = "JLog"
  val Log4j2             = "Log4j2"
  val Slf4j              = "Slf4j"
  val ScalaLoggingLazy   = "ScalaLoggingLazy"
  val ScalaLoggingStrict = "ScalaLoggingStrict"

  private lazy val types: Map[String, BaseLog] = Map(
    JLogImpl.`type`               -> JLogImpl,
    Log4J2Impl.`type`             -> Log4J2Impl,
    Slf4jImpl.`type`              -> Slf4jImpl,
    ScalaLoggingStrictImpl.`type` -> ScalaLoggingStrictImpl,
    ScalaLoggingLazyImpl.`type`   -> ScalaLoggingLazyImpl
  ).map(kv => kv._1.toLowerCase -> kv._2)

  val values = types.keySet

  def getLogImpl(logType: String): BaseLog =
    types.getOrElse(
      logType.toLowerCase,
      default = throw new Exception(s"$logType is not in the supported list: ${values.mkString(",")}")
    )

}
