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

package io.github.dreamylost

import io.github.dreamylost.macros.jacksonEnumMacro

import scala.annotation.{ compileTimeOnly, StaticAnnotation }

/**
 * annotation to generate equals and hashcode method for classes.
 *
 * @author 梦境迷离
 * @param verbose       Whether to enable detailed log.
 * @param nonTypeRefers Whether to not generate the subclass of the TypeReference for paramTypes of class.
 * @since 2021/7/18
 * @version 1.0
 */
@compileTimeOnly("enable macro to expand macro annotations")
final class jacksonEnum(
  verbose: Boolean = false,
  nonTypeRefers: Seq[String] = Nil
) extends StaticAnnotation {

  def macroTransform(annottees: Any*): Any = macro jacksonEnumMacro.JacksonEnumProcessor.impl

}
