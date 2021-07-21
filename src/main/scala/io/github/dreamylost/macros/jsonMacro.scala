/*
 * Copyright (c) 2021 jxnu-liguobin && contributors
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

package io.github.dreamylost.macros

import scala.reflect.macros.whitebox

/**
 *
 * @author 梦境迷离
 * @since 2021/7/7
 * @version 1.0
 */
object jsonMacro extends MacroCommon {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def jsonFormatter(className: TypeName, fields: List[Tree]): c.universe.Tree = {
      fields.length match {
        case 0 => c.abort(c.enclosingPosition, "Cannot create json formatter for case class with no fields")
        case _ =>
          c.info(c.enclosingPosition, s"jsonFormatter className: $className, field length: ${fields.length}", force = true)
          q"implicit val jsonAnnotationFormat = play.api.libs.json.Json.format[$className]"
      }
    }

    // The dependent type need aux-pattern in scala2. Now let's get around this.
    def modifiedDeclaration(classDecl: ClassDef, compDeclOpt: Option[ModuleDef] = None): Any = {
      val (className, fields) = classDecl match {
        case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends ..$bases { ..$body }" =>
          if (!mods.asInstanceOf[Modifiers].hasFlag(Flag.CASE)) {
            c.abort(c.enclosingPosition, s"Annotation is only supported on case class. classDef: $classDecl, mods: $mods")
          } else {
            c.info(c.enclosingPosition, s"modifiedDeclaration className: $tpname, paramss: $paramss", force = true)
            (tpname, paramss.asInstanceOf[List[List[Tree]]])
          }
        case _ => c.abort(c.enclosingPosition, s"${ErrorMessage.ONLY_CLASS} classDef: $classDecl")
      }
      c.info(c.enclosingPosition, s"modifiedDeclaration className: $className, fields: $fields", force = true)
      val cName = className.asInstanceOf[TypeName]
      val format = jsonFormatter(cName, fields.flatten)
      val compDecl = modifiedCompanion(c)(compDeclOpt, format, cName)
      c.info(c.enclosingPosition, s"format: $format, compDecl: $compDecl", force = true)
      // Return both the class and companion object declarations
      c.Expr(
        q"""
        $classDecl
        $compDecl
      """)

    }

    c.info(c.enclosingPosition, s"json annottees: $annottees", force = true)
    val resTree = handleWithImplType(c)(annottees: _*)(modifiedDeclaration)
    printTree(c)(force = true, resTree.tree)

    resTree
  }
}
