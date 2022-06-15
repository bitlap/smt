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

package org.bitlap.csv.derive

import scala.reflect.macros.blackbox
import org.bitlap.common.AbstractMacroProcessor
import org.bitlap.csv.{ Converter, CsvFormat }

/** This is a tool macro for automatic derivation of the base CSV converter.
 *
 *  @author
 *    梦境迷离
 *  @version 1.0,2022/4/29
 */
object DeriveCsvConverter {

  def gen[CC](implicit csvFormat: CsvFormat): Converter[CC] = macro Macro.macroImpl[CC]

  class Macro(override val c: blackbox.Context) extends AbstractMacroProcessor(c) {
    import c.universe._
    protected val packageName = q"_root_.org.bitlap.csv"

    private val lineTermName = TermName("line")
    private val tTermName    = TermName("t")

    def macroImpl[CC: c.WeakTypeTag](csvFormat: c.Expr[CsvFormat]): c.Expr[CC] = {
      val clazzName = c.weakTypeOf[CC].typeSymbol.name
      val typeName  = TypeName(clazzName.decodedName.toString)
      val tree =
        q"""
        new Converter[$typeName] {
            override def toScala($lineTermName: String): _root_.scala.Option[$typeName] = $packageName.macros.DeriveToCaseClass[$typeName]($lineTermName)($csvFormat)
            override def toCsvString($tTermName: $typeName): String = $packageName.macros.DeriveToString[$typeName]($tTermName)($csvFormat)
        }
       """
      exprPrintTree[CC](force = false, tree)
    }
  }
}
