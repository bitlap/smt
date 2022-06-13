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

package org.bitlap.common

import org.bitlap.common.CaseClassExtractor.exprPrintTree

import scala.reflect.macros.whitebox
import scala.collection.Seq

trait CaseClassField {

  def stringify: String

  type Field
}

object CaseClassField {

  final val classNameTermName = "CaseClassField"
  final val stringifyTermName = "stringify"
  final val fieldTermName     = "Field"

  def apply[T <: Product](field: T => Any): CaseClassField = macro selectFieldMacroImpl[T]

  def selectFieldMacroImpl[T: c.WeakTypeTag](
    c: whitebox.Context
  )(field: c.Expr[T => Any]): c.Expr[CaseClassField] = {
    import c.universe._
    val packageName                      = q"_root_.org.bitlap.common"
    val Function(_, Select(_, termName)) = field.tree
    val caseClassParams                  = getCaseClassParams[T](c)
    val fieldName                        = termName.decodedName.toString
    val searchField =
      caseClassParams.find(_.name.toTermName.decodedName.toString == fieldName)
    val fieldType = searchField.map(f => c.typecheck(tq"$f", c.TYPEmode).tpe)
    if (searchField.isEmpty || fieldType.isEmpty) {
      c.abort(
        c.enclosingPosition,
        s"""Field name is invalid, "${c.weakTypeOf[T].resultType}" does not have a field named $fieldName! 
           |Please consider using "CaseClassField[T]($fieldName)" instead of "CaseClassField($fieldName)" """.stripMargin
      )
    }

    val genericType = fieldType.get match {
      case t if t <:< typeOf[Option[_]] =>
        val genericType = t.typeArgs.head
        tq"Option[$genericType]"
      case t if t <:< typeOf[Seq[_]] =>
        val genericType = t.typeArgs.head
        tq"Seq[$genericType]"
      case t if t <:< typeOf[List[_]] =>
        val genericType = t.typeArgs.head
        tq"List[$genericType]"
      case t => tq"$t"
    }

    val fieldNameTypeName = TermName(s"${CaseClassField.classNameTermName}$$$fieldName")
    val res = q"""
       case object $fieldNameTypeName extends $packageName.${TypeName(CaseClassField.classNameTermName)} {
          override def ${TermName(CaseClassField.stringifyTermName)}: String = $fieldName
          override type ${TypeName(CaseClassField.fieldTermName)} = $genericType
       }
     $fieldNameTypeName
     """
    exprPrintTree[CaseClassField](c)(res)
  }

  def getCaseClassParams[T: c.WeakTypeTag](c: whitebox.Context): List[c.Symbol] = {
    import c.universe._
    val parameters = c.weakTypeOf[T].resultType.member(TermName("<init>")).typeSignature.paramLists
    if (parameters.size > 1) {
      c.abort(c.enclosingPosition, "The constructor of case class has currying!")
    }
    parameters.flatten
  }

}
