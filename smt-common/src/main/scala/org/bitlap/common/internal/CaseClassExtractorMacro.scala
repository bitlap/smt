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

package org.bitlap.common.internal

import org.bitlap.common.CaseClassField

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import scala.reflect.macros.whitebox

/** @author
 *    梦境迷离
 *  @version 1.0,6/27/22
 */
object CaseClassExtractorMacro {

  def macroImpl[T](
    c: whitebox.Context
  )(t: c.Expr[T], field: c.Expr[CaseClassField]): c.Expr[Option[Any]] = {
    import c.universe._
    // scalafmt: { maxColumn = 400 }
    val tree =
      q"""
       if ($t == null) _root_.scala.None else {
          val _field = $field
          _field.${TermName(CaseClassField.fieldNamesTermName)}.find(kv => kv._2 == _field.${TermName(CaseClassField.stringifyTermName)})
          .map(kv => $t.productElement(kv._1))       
       }
     """
    c.info(
      c.enclosingPosition,
      s"\n###### Time: ${ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)} Expanded macro start ######\n" + tree
        .toString() + "\n###### Expanded macro end ######\n",
      force = false
    )
    c.Expr[Option[Any]](tree)
  }
}
