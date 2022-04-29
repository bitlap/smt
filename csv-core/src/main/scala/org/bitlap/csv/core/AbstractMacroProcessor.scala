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

package org.bitlap.csv.core

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import scala.reflect.macros.blackbox

/**
 *
 * @author 梦境迷离
 * @since 2021/7/24
 * @version 1.0
 */
abstract class AbstractMacroProcessor(val c: blackbox.Context) {

  def printTree[T: c.WeakTypeTag](c: blackbox.Context)(force: Boolean, resTree: c.Tree): c.Expr[T] = {
    c.info(
      c.enclosingPosition,
      s"\n###### Time: ${
        ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
      } " +
        s"Expanded macro start ######\n" + resTree.toString() + "\n###### Expanded macro end ######\n",
      force = force
    )
    c.Expr[T](resTree)
  }
}
