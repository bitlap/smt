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

package org.bitlap.tools.internal

import scala.reflect.macros.whitebox

/** @author
 *    梦境迷离
 *  @since 2021/7/7
 *  @version 1.0
 */
object applyMacro {

  class applyProcessor(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

    import c.universe._

    /** We generate apply method with currying, and we have to deal with the first layer of currying alone.
     *
     *  @param typeName
     *  @param fieldss
     *  @return
     *    A apply method with currying.
     *  @example
     *    Return a tree, such as `def apply(int: Int)(j: Int)(k: Option[String])(t: Option[Long]): B3 = new
     *    B3(int)(j)(k)(t)`
     */
    private def getApplyMethodWithCurrying(
      typeName: TypeName,
      fieldss: List[List[Tree]],
      classTypeParams: List[Tree]
    ): Tree = {
      val allFieldsTermName = fieldss.map(f => getConstructorParamsNameWithType(f))
      val returnTypeParams  = extractClassTypeParamsTypeName(classTypeParams)
      // not currying
      val applyMethod = if (fieldss.isEmpty || fieldss.size == 1) {
        q"def apply[..$classTypeParams](..${allFieldsTermName.flatten}): $typeName[..$returnTypeParams] = ${getConstructorWithCurrying(typeName, fieldss, isCase = false)}"
      } else {
        // currying
        val first = allFieldsTermName.head
        q"def apply[..$classTypeParams](..$first)(...${allFieldsTermName.tail}): $typeName[..$returnTypeParams] = ${getConstructorWithCurrying(typeName, fieldss, isCase = false)}"
      }
      applyMethod
    }

    override def createCustomExpr(classDecl: ClassDef, compDeclOpt: Option[ModuleDef] = None): Any = {
      val classDefinition = mapToClassDeclInfo(classDecl)
      val apply = getApplyMethodWithCurrying(
        classDefinition.className,
        classDefinition.classParamss,
        classDefinition.classTypeParams
      )
      val compDecl = appendModuleBody(compDeclOpt, List(apply), classDefinition.className)
      c.Expr(q"""
            $classDecl
            $compDecl
          """)
    }

    override def checkAnnottees(annottees: Seq[c.universe.Expr[Any]]): Unit = {
      super.checkAnnottees(annottees)
      val annotateeClass: ClassDef = checkGetClassDef(annottees)
      if (isCaseClass(annotateeClass)) {
        c.abort(c.enclosingPosition, ErrorMessage.ONLY_CASE_CLASS)
      }
    }
  }

}
