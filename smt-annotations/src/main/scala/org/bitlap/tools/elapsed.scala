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

package org.bitlap.tools

import org.bitlap.tools.LogLevel.LogLevel
import org.bitlap.tools.macros.elapsedMacro.ElapsedProcessor

import scala.annotation.{ compileTimeOnly, StaticAnnotation }
import scala.concurrent.duration.Duration

/** annotation to record method cost time.
 *
 *  @author
 *    梦境迷离
 *  @param limit
 *    Time consuming condition to trigger log.
 *  @param logLevel
 *    Log Level.
 *  @since 2021/8/7
 *  @version 1.0
 */
@compileTimeOnly("enable macro to expand macro annotations")
final class elapsed(limit: Duration, logLevel: LogLevel) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro ElapsedProcessor.impl
}
