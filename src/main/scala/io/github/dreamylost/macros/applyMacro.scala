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
object applyMacro {

  class applyProcessor(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

    import c.universe._

    private val extractArgumentsDetail: Tuple1[Boolean] = {
      extractArgumentsTuple1 {
        case q"new apply(verbose=$verbose)" => Tuple1(evalTree(verbose.asInstanceOf[Tree]))
        case q"new apply()"                 => Tuple1(false)
        case _                              => c.abort(c.enclosingPosition, ErrorMessage.UNEXPECTED_PATTERN)
      }
    }

    override def createCustomExpr(classDecl: ClassDef, compDeclOpt: Option[ModuleDef] = None): Any = {
      val classDefinition = mapToClassDeclInfo(classDecl)
      val apply = getApplyMethodWithCurrying(classDefinition.className, classDefinition.classParamss, classDefinition.classTypeParams)
      val compDecl = appendModuleBody(compDeclOpt, List(apply), classDefinition.className)
      c.Expr(
        q"""
            $classDecl
            $compDecl
          """)
    }

    override def impl(annottees: Expr[Any]*): Expr[Any] = {
      val annotateeClass: ClassDef = checkAndGetClassDef(annottees)
      if (isCaseClass(annotateeClass)) {
        c.abort(c.enclosingPosition, ErrorMessage.ONLY_CASE_CLASS)
      }
      val resTree = collectCustomExpr(annottees)(createCustomExpr)
      printTree(force = extractArgumentsDetail._1, resTree.tree)
      resTree
    }
  }

}
