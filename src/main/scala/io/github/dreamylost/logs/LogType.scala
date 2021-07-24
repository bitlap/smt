/*
 * Copyright (c) 2021 jxnu-liguobin && contributors
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

package io.github.dreamylost.logs

import io.github.dreamylost.PACKAGE

object LogType extends Enumeration {

  type LogType = Value
  val JLog, Log4j2, Slf4j = Value

  private lazy val types = Map(
    JLog -> JLogImpl,
    Log4j2 -> Log4J2Impl,
    Slf4j -> Slf4jImpl
  )

  def getLogImpl(logType: LogType): BaseLog = {
    types.getOrElse(logType, default = throw new Exception(s"Not support log type: $logType"))
  }

  def getLogType(shortType: String): LogType = {
    val tpe1 = s"$PACKAGE.logs.$shortType" //LogType.JLog
    val tpe2 = s"$PACKAGE.logs.LogType.$shortType" // JLog
    val v = LogType.values.find(p => {
      s"$PACKAGE.logs.LogType.${p.toString}" == tpe1 ||
        s"$PACKAGE.logs.LogType.${p.toString}" == tpe2 || s"$PACKAGE.logs.LogType.${p.toString}" == shortType
    }).getOrElse(throw new Exception(s"Not support log type: $shortType")).toString
    LogType.withName(v)
  }
}

