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

package org.bitlap.tools.logs.impl

import org.bitlap.tools.logs.{ BaseLog, LogArgument, LogType }
import org.bitlap.tools.logs.LogType.LogType

import scala.reflect.macros.whitebox

/**
 * @author 梦境迷离
 * @version 1.0,2022/3/29
 */
object Log4J2Impl extends BaseLog {

  override val `type`: LogType = LogType.Log4j2

  override def getTemplate(c: whitebox.Context)(logArgument: LogArgument): c.Tree = {
    import c.universe._
    if (logArgument.isClass) {
      q"""@transient private final val log: org.apache.logging.log4j.Logger = org.apache.logging.log4j.LogManager.getLogger(classOf[${TypeName(
        logArgument.classNameStr
      )}].getName)"""
    } else {
      q"""@transient private final val log: org.apache.logging.log4j.Logger = org.apache.logging.log4j.LogManager.getLogger(${TermName(
        logArgument.classNameStr
      )}.getClass.getName)"""
    }
  }

}
