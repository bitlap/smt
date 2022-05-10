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

import scala.reflect.macros.blackbox

/**
 * @author 梦境迷离
 * @version 1.0,2022/4/29
 */
object DeriveToString {

  def apply[T <: Product](t: T, columnSeparator: Char): String = macro Macro.macroImpl[T]

  class Macro(override val c: blackbox.Context) extends AbstractMacroProcessor(c) {

    import c.universe._

    def macroImpl[T: c.WeakTypeTag](t: c.Expr[T], columnSeparator: c.Expr[Char]): c.Expr[String] = {
      val (names, indexTypes) = super.checkCaseClassZip[T]
      val clazzName = c.weakTypeOf[T].typeSymbol.name
      val innerVarTermName = TermName("_t")
      val indexByName = (i: Int) => TermName(names(i))
      val fieldsToString = indexTypes.map { idxType =>
        if (idxType._2 <:< typeOf[Option[_]]) {
          val genericType = c.typecheck(q"${idxType._2}", c.TYPEmode).tpe.typeArgs.head
          q"""$packageName.Converter[${genericType.typeSymbol.name.toTypeName}].toCsvString { 
                  if ($innerVarTermName.${indexByName(idxType._1)}.isEmpty) "" else $innerVarTermName.${indexByName(
            idxType._1
          )}.get
                }
                """
        } else {
          q"$packageName.Converter[${TypeName(idxType._2.typeSymbol.name.decodedName.toString)}].toCsvString($innerVarTermName.${indexByName(idxType._1)})"
        }
      }
      val separator = q"$columnSeparator"
      val tree =
        q"""
        val $innerVarTermName = $t    
        val fields = ${TermName(clazzName.decodedName.toString)}.unapply($innerVarTermName).orNull
        if (null == fields) "" else $fieldsToString.mkString($separator.toString)
       """
      exprPrintTree[String](force = false, tree)
    }

  }

}
