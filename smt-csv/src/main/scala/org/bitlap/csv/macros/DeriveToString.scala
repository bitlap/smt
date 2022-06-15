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

package org.bitlap.csv.macros

import org.bitlap.common.AbstractMacroProcessor
import org.bitlap.csv.CsvFormat

import scala.reflect.macros.blackbox
import org.bitlap.csv.CsvFormat

/** @author
 *    梦境迷离
 *  @version 1.0,2022/4/29
 */
object DeriveToString {

  def apply[T <: Product](t: T)(implicit csvFormat: CsvFormat): String = macro Macro.macroImpl[T]

  class Macro(override val c: blackbox.Context) extends AbstractMacroProcessor(c) {

    import c.universe._
    protected val packageName = q"_root_.org.bitlap.csv"

    def macroImpl[T: c.WeakTypeTag](t: c.Expr[T])(csvFormat: c.Expr[CsvFormat]): c.Expr[String] = {
      val (names, indexTypes) = super.checkCaseClassZipParams[T]
      val clazzName           = c.weakTypeOf[T].typeSymbol.name
      val innerVarTermName    = TermName("_t")
      val indexByName         = (i: Int) => TermName(names(i))
      val fieldsToString = indexTypes.map { idxType =>
        if (idxType._2 <:< typeOf[Option[_]]) {
          val genericType = c.typecheck(q"${idxType._2}", c.TYPEmode).tpe.typeArgs.head
          // scalafmt: { maxColumn = 400 }
          q"""$packageName.Converter[${genericType.typeSymbol.name.toTypeName}].toCsvString { 
                  if ($innerVarTermName.${indexByName(idxType._1)}.isEmpty) "" else $innerVarTermName.${indexByName(idxType._1)}.get
              }
          """
        } else {
          idxType._2 match {
            case t if t <:< typeOf[List[_]] =>
              val genericType = c.typecheck(q"${idxType._2}", c.TYPEmode).tpe.typeArgs.head
              q"$packageName.Converter[List[${TypeName(genericType.typeSymbol.name.decodedName.toString)}]].toCsvString($innerVarTermName.${indexByName(idxType._1)})"
            case t if t <:< typeOf[Seq[_]] =>
              val genericType = c.typecheck(q"${idxType._2}", c.TYPEmode).tpe.typeArgs.head
              q"$packageName.Converter[Seq[${TypeName(genericType.typeSymbol.name.decodedName.toString)}]].toCsvString($innerVarTermName.${indexByName(idxType._1)})"
            case _ =>
              q"$packageName.Converter[${TypeName(idxType._2.typeSymbol.name.decodedName.toString)}].toCsvString($innerVarTermName.${indexByName(idxType._1)})"
          }
        }
      }
      val tree =
        q"""
        val $innerVarTermName = $t    
        val fields = ${TermName(clazzName.decodedName.toString)}.unapply($innerVarTermName).orNull
        val values = if (null == fields) List.empty else $fieldsToString
        $packageName.StringUtils.combineColumns(values, $csvFormat)
       """
      exprPrintTree[String](force = false, tree)
    }

  }

}
