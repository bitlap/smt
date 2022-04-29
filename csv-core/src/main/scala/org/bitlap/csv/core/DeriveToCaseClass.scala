package org.bitlap.csv.core

import scala.reflect.macros.blackbox

/**
 * @author 梦境迷离
 * @version 1.0,2022/4/29
 */
object DeriveToCaseClass {

  def apply[T <: Product](line: String, columnSeparator: String): Option[T] = macro Macro.macroImpl[T]

  class Macro(override val c: blackbox.Context) extends AbstractMacroProcessor(c) {
    def macroImpl[T <: Product : c.WeakTypeTag](line: c.Expr[String], columnSeparator: c.Expr[String]): c.Expr[Option[T]] = {
      import c.universe._
      val parameters = c.weakTypeOf[T].resultType.member(TermName("<init>")).typeSignature.paramLists
      if (parameters.size > 1) {
        c.abort(c.enclosingPosition, "The parameters list in constructor of case cass have currying!")
      }
      lazy val columns = q"$line.split($columnSeparator)"
      val params = parameters.flatten
      val paramsSize = params.size
      val clazzName = c.weakTypeOf[T].typeSymbol.name
      val types = params.zip(0 until paramsSize).map(f => c.typecheck(tq"${f._1}", c.TYPEmode).tpe)
      val index = (0 until paramsSize).toList.map(i => q"$columns($i)")
      val fields = (index zip types).map { f =>
        if (f._2 <:< typeOf[Option[_]]) {
          val genericType = c.typecheck(q"${f._2}", c.TYPEmode).tpe.typeArgs.head
          q"CsvConverter[${genericType.typeSymbol.name.toTypeName}].from(${f._1})"
        } else {
          f._2 match {
            case t if t <:< typeOf[Int] =>
              q"CsvConverter[${TypeName(f._2.typeSymbol.name.decodedName.toString)}].from(${f._1}).getOrElse(0)"
            case t if t <:< typeOf[String] =>
              q"""CsvConverter[${TypeName(f._2.typeSymbol.name.decodedName.toString)}].from(${f._1}).getOrElse("")"""
            case t if t <:< typeOf[Double] =>
              q"CsvConverter[${TypeName(f._2.typeSymbol.name.decodedName.toString)}].from(${f._1}).getOrElse(0D)"
            case t if t <:< typeOf[Float] =>
              q"CsvConverter[${TypeName(f._2.typeSymbol.name.decodedName.toString)}].from(${f._1}).getOrElse(0F)"
            case t if t <:< typeOf[Char] =>
              q"CsvConverter[${TypeName(f._2.typeSymbol.name.decodedName.toString)}].from(${f._1}).getOrElse('0')"
            case t if t <:< typeOf[Byte] =>
              q"CsvConverter[${TypeName(f._2.typeSymbol.name.decodedName.toString)}].from(${f._1}).getOrElse(0)"
            case t if t <:< typeOf[Short] =>
              q"CsvConverter[${TypeName(f._2.typeSymbol.name.decodedName.toString)}].from(${f._1}).getOrElse(0)"
            case t if t <:< typeOf[Boolean] =>
              q"CsvConverter[${TypeName(f._2.typeSymbol.name.decodedName.toString)}].from(${f._1}).getOrElse(false)"
            case t if t <:< typeOf[Long] =>
              q"CsvConverter[${TypeName(f._2.typeSymbol.name.decodedName.toString)}].from(${f._1}).getOrElse(0L)"
          }
        }

      }
      val tree =
        q"""
       Option(${TermName(clazzName.decodedName.toString)}(..$fields))
     """
      printTree[T](c)(force = true, tree)

    }.asInstanceOf[c.Expr[Option[T]]]

  }
}
