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
object jsonMacro {

  class JsonProcessor(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

    import c.universe._

    private def jsonFormatter(className: TypeName, fields: List[Tree]): Tree = {
      fields.length match {
        case 0 => c.abort(c.enclosingPosition, "Cannot create json formatter for case class with no fields")
        case _ =>
          q"implicit val jsonAnnotationFormat = play.api.libs.json.Json.format[$className]"
      }
    }

    override def checkAnnottees(annottees: Seq[c.universe.Expr[Any]]): Unit = {
      super.checkAnnottees(annottees)
      val annotateeClass: ClassDef = checkGetClassDef(annottees)
      if (!isCaseClass(annotateeClass)) {
        c.abort(c.enclosingPosition, ErrorMessage.ONLY_CASE_CLASS)
      }
    }

    override def createCustomExpr(classDecl: c.universe.ClassDef, compDeclOpt: Option[c.universe.ModuleDef]): Any = {
      val classDefinition = mapToClassDeclInfo(classDecl)
      val format = jsonFormatter(classDefinition.className, classDefinition.classParamss.flatten)
      val compDecl = appendModuleBody(compDeclOpt, List(format), classDefinition.className)
      // Return both the class and companion object declarations
      c.Expr(
        q"""
        $classDecl
        $compDecl
      """)
    }
  }

}
