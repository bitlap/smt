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
object DeriveToCaseClass {

  def apply[T <: Product](line: String, columnSeparator: Char): Option[T] = macro Macro.macroImpl[T]

  class Macro(override val c: blackbox.Context) extends AbstractMacroProcessor(c) {
    def macroImpl[T <: Product: c.WeakTypeTag](
      line:            c.Expr[String],
      columnSeparator: c.Expr[Char]
    ): c.Expr[Option[T]] = {
      import c.universe._
      val clazzName = c.weakTypeOf[T].typeSymbol.name
      val fields = checkCaseClassReturnConstructorParams[T](line, columnSeparator).map { idxType =>
        if (idxType._2 <:< typeOf[Option[_]]) {
          val genericType = c.typecheck(q"${idxType._2}", c.TYPEmode).tpe.typeArgs.head
          q"Converter[${genericType.typeSymbol.name.toTypeName}].toScala(${idxType._1})"
        } else {
          idxType._2 match {
            case t if t =:= typeOf[Int] =>
              q"Converter[${TypeName(idxType._2.typeSymbol.name.decodedName.toString)}].toScala(${idxType._1}).getOrElse(0)"
            case t if t =:= typeOf[String] =>
              q"""Converter[${TypeName(idxType._2.typeSymbol.name.decodedName.toString)}].toScala(${idxType._1}).getOrElse("")"""
            case t if t =:= typeOf[Float] =>
              q"Converter[${TypeName(idxType._2.typeSymbol.name.decodedName.toString)}].toScala(${idxType._1}).getOrElse(0F)"
            case t if t =:= typeOf[Double] =>
              q"Converter[${TypeName(idxType._2.typeSymbol.name.decodedName.toString)}].toScala(${idxType._1}).getOrElse(0D)"
            case t if t =:= typeOf[Char] =>
              q"Converter[${TypeName(idxType._2.typeSymbol.name.decodedName.toString)}].toScala(${idxType._1}).getOrElse('?')"
            case t if t =:= typeOf[Byte] =>
              q"Converter[${TypeName(idxType._2.typeSymbol.name.decodedName.toString)}].toScala(${idxType._1}).getOrElse(0)"
            case t if t =:= typeOf[Short] =>
              q"Converter[${TypeName(idxType._2.typeSymbol.name.decodedName.toString)}].toScala(${idxType._1}).getOrElse(0)"
            case t if t =:= typeOf[Boolean] =>
              q"Converter[${TypeName(idxType._2.typeSymbol.name.decodedName.toString)}].toScala(${idxType._1}).getOrElse(false)"
            case t if t =:= typeOf[Long] =>
              q"Converter[${TypeName(idxType._2.typeSymbol.name.decodedName.toString)}].toScala(${idxType._1}).getOrElse(0L)"
          }
        }
      }
      val tree =
        q"""
       Option(${TermName(clazzName.decodedName.toString)}(..$fields))
     """
      printTree[T](force = true, tree)

    }.asInstanceOf[c.Expr[Option[T]]]

  }
}
