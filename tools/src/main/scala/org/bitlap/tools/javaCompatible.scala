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

import scala.annotation.{ compileTimeOnly, StaticAnnotation }
import org.bitlap.tools.macros.javaCompatibleMacro

/**
 * annotation to generate non-parameter constructor and get/set method for case classes.
 * Fields marked `private[this]` in curry are not supported !
 *
 * @author 梦境迷离
 * @param verbose Whether to enable detailed log.
 * @since 2021/11/23
 * @version 1.0
 */
@compileTimeOnly("enable macro to expand macro annotations")
final class javaCompatible(verbose: Boolean = false) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro javaCompatibleMacro.JavaCompatibleProcessor.impl
}
