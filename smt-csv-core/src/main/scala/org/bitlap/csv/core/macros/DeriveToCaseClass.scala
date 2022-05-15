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

    import c.universe._

    // scalafmt: { maxColumn = 400 }
    def macroImpl[T <: Product: c.WeakTypeTag](line: c.Expr[String], columnSeparator: c.Expr[Char]): c.Expr[Option[T]] = {
      val clazzName = c.weakTypeOf[T].typeSymbol.name
      val innerFuncTermName = TermName("_columns")
      val fields = (columnsFunc: TermName) =>
        checkCaseClassZipAll[T](columnsFunc).map { idxType =>
          val columnValues = idxType._1._2
          idxType._2 match {
            case t if t <:< typeOf[Option[_]] =>
              val genericType = c.typecheck(q"${idxType._2}", c.TYPEmode).tpe.typeArgs.head
              q"$packageName.Converter[${genericType.typeSymbol.name.toTypeName}].toScala($columnValues)"
            case t if t <:< typeOf[List[_]] =>
              val genericType = c.typecheck(q"${idxType._2}", c.TYPEmode).tpe.typeArgs.head
              q"$packageName.Converter[List[${genericType.typeSymbol.name.toTypeName}]].toScala($columnValues).getOrElse(Nil)"
            case t if t <:< typeOf[Seq[_]] =>
              val genericType = c.typecheck(q"${idxType._2}", c.TYPEmode).tpe.typeArgs.head
              q"$packageName.Converter[Seq[${genericType.typeSymbol.name.toTypeName}]].toScala($columnValues).getOrElse(Nil)"
            case t =>
              val caseClassFieldTypeName = TypeName(idxType._2.typeSymbol.name.decodedName.toString)
              t match {
                case tt if tt =:= typeOf[Int] =>
                  q"$packageName.Converter[$caseClassFieldTypeName].toScala($columnValues).getOrElse(0)"
                case tt if tt =:= typeOf[String] =>
                  q"""$packageName.Converter[$caseClassFieldTypeName].toScala($columnValues).getOrElse("")"""
                case tt if tt =:= typeOf[Float] =>
                  q"$packageName.Converter[$caseClassFieldTypeName].toScala($columnValues).getOrElse(0F)"
                case tt if tt =:= typeOf[Double] =>
                  q"$packageName.Converter[$caseClassFieldTypeName].toScala($columnValues).getOrElse(0D)"
                case tt if tt =:= typeOf[Char] =>
                  q"$packageName.Converter[$caseClassFieldTypeName].toScala($columnValues).getOrElse('?')"
                case tt if tt =:= typeOf[Byte] =>
                  q"$packageName.Converter[$caseClassFieldTypeName].toScala($columnValues).getOrElse(0)"
                case tt if tt =:= typeOf[Short] =>
                  q"$packageName.Converter[$caseClassFieldTypeName].toScala($columnValues).getOrElse(0)"
                case tt if tt =:= typeOf[Boolean] =>
                  q"$packageName.Converter[$caseClassFieldTypeName].toScala($columnValues).getOrElse(false)"
                case tt if tt =:= typeOf[Long] =>
                  q"$packageName.Converter[$caseClassFieldTypeName].toScala($columnValues).getOrElse(0L)"
                case _ =>
                  q"$packageName.Converter[$caseClassFieldTypeName].toScala($columnValues).getOrElse(null)"
              }
          }
        }
      val tree =
        q"""
           lazy val $innerFuncTermName = () => $packageName.StringUtils.splitColumns($line, $columnSeparator)
           Option(${TermName(clazzName.decodedName.toString)}(..${fields(innerFuncTermName)}))
           """
      exprPrintTree[T](force = false, tree)

    }.asInstanceOf[c.Expr[Option[T]]]

  }
}
