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

import scala.reflect.macros.blackbox

/**
 * @author 梦境迷离
 * @version 1.0,2022/4/29
 */
object DeriveToString {

  def apply[T <: Product](t: T): String = macro Macro.macroImpl[T]

  class Macro(override val c: blackbox.Context) extends AbstractMacroProcessor(c) {

    def macroImpl[T <: Product: c.WeakTypeTag](t: c.Expr[T]): c.Expr[String] = {
      val clazzName = c.weakTypeOf[T].typeSymbol.name
      import c.universe._
      val tree =
        q"""
        val fields = ${TermName(clazzName.decodedName.toString)}.unapply($t).orNull
        val fieldsStr = if (null == fields) fields.toString() else ""
        fieldsStr.replace("(", "").replace(")", "")
       """

      printTree[String](c)(force = true, tree)
    }.asInstanceOf[c.Expr[String]]
  }

}
