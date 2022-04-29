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
