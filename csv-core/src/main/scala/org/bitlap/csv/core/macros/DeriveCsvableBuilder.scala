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

package org.bitlap.csv.core.macros

import org.bitlap.csv.core.{ Csvable, CsvableBuilder }

import scala.reflect.macros.whitebox

class DeriveCsvableBuilder(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

  import c.universe._

  private val packageName = q"_root_.org.bitlap.csv.core"

  private val annoBuilderPrefix = "AnonCsvableBuilder$"

  def applyImpl[T: c.WeakTypeTag]: c.Expr[CsvableBuilder[T]] = {
    deriveBuilderApplyImpl[T]
  }

  def buildImpl[T: c.WeakTypeTag](columnSeparator: c.Expr[Char]): c.Expr[Csvable[T]] = {
    deriveCsvableImpl[T](columnSeparator)
  }

  private def deriveBuilderApplyImpl[T: WeakTypeTag]: c.Expr[CsvableBuilder[T]] = {
    val className = TypeName(annoBuilderPrefix + MacroCache.getBuilderId)
    val caseClazzName = TypeName(c.weakTypeOf[T].typeSymbol.name.decodedName.toString)
    val tree =
      q"""
       class $className extends $packageName.CsvableBuilder[$caseClazzName]
       new $className
     """
    printTree[CsvableBuilder[T]](force = true, tree)
  }

  private def deriveCsvableImpl[T: c.WeakTypeTag](columnSeparator: c.Expr[Char]): c.Expr[Csvable[T]] = {
    val clazzName = TypeName(c.weakTypeOf[T].typeSymbol.name.decodedName.toString)
    val packTermName = TermName("org.bitlap.csv.core.Csvable")
    val tree =
      q"""
       new $packageName.Csvable[$clazzName] {
          override def toCsvString(t: $clazzName): String = ${stringMacroImpl[T](c.Expr(q"${TermName("t")}"), columnSeparator, packTermName)}
       }
    """
    printTree[Csvable[T]](force = true, tree)
  }
}
