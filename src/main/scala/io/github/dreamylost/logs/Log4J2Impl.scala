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

import io.github.dreamylost.logs.LogType.LogType

import scala.reflect.macros.whitebox

object Log4J2Impl extends BaseLog {

  override val typ: LogType = LogType.Log4j2

  override def getTemplate(c: whitebox.Context)(t: String, isClass: Boolean): c.Tree = {
    import c.universe._
    if (isClass) {
      q"""private final val log: org.apache.logging.log4j.Logger = org.apache.logging.log4j.LogManager.getLogger(classOf[${TypeName(t)}].getName)"""
    } else {
      q"""private final val log: org.apache.logging.log4j.Logger = org.apache.logging.log4j.LogManager.getLogger(${TermName(t)}.getClass.getName)"""
    }
  }

}
