/*
 * Copyright (c) 2022 org.bitlap
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

package org.bitlap.tools.macros

import scala.reflect.macros.whitebox

/**
 *
 * @author 梦境迷离
 * @since 2021/11/23
 * @version 1.0
 */
object javaCompatibleMacro {

  class JavaCompatibleProcessor(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

    import c.universe._

    /**
     * We generate this method with currying, and we have to deal with the first layer of currying alone.
     */
    private def getNoArgsContrWithCurrying(annotteeClassParams: List[List[Tree]], annotteeClassDefinitions: Seq[Tree]): Tree = {
      if (annotteeClassDefinitions.exists(f => !isNotLocalClassMember(f))) {
        c.info(c.enclosingPosition, s"The params of 'private[this]' exists in class constructor", verbose)
      }
      annotteeClassDefinitions.foreach {
        case defDef: DefDef if defDef.name.decodedName.toString == "this" && defDef.vparamss.isEmpty =>
          c.abort(defDef.pos, "Non-parameter constructor method has already defined, please remove it or not use'@JavaCompatible'")
        case _ =>
      }

      val defaultParameters = annotteeClassParams.map(valDefAccessors).map(params => params.map(param => {
        param.paramType match {
          case t if t <:< typeOf[Int]     => q"0"
          case t if t <:< typeOf[Byte]    => q"0"
          case t if t <:< typeOf[Double]  => q"0D"
          case t if t <:< typeOf[Float]   => q"0F"
          case t if t <:< typeOf[Short]   => q"0"
          case t if t <:< typeOf[Long]    => q"0L"
          case t if t <:< typeOf[Char]    => q"63.toChar" // default char is ?
          case t if t <:< typeOf[Boolean] => q"false"
          case _                          => q"null"
        }
      }))
      if (annotteeClassParams.isEmpty || annotteeClassParams.size == 1) {
        q"""
          def this() = {
            this(..${defaultParameters.flatten})
          }
         """
      } else {
        q"""
          def this() = {
            this(..${defaultParameters.head})(...${defaultParameters.tail})
          }
         """
      }
    }

    private def replaceAnnotation(valDefTree: Tree): Tree = {
      val safeValDef = valDefAccessors(Seq(valDefTree)).head
      val mods = safeValDef.mods.mapAnnotations(f => {
        if (!f.toString().contains("BeanProperty")) f ++ List(q"new _root_.scala.beans.BeanProperty") else f
      })
      ValDef(mods, safeValDef.name, safeValDef.tpt, safeValDef.rhs)
    }

    private def getClassWithBeanProperty(classDecl: ClassDef): Tree = {
      val q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends ..$bases { ..$body }" = classDecl
      val newFieldss = paramss.asInstanceOf[List[List[Tree]]].map(_.map(replaceAnnotation))
      q"$mods class $tpname[..$tparams] $ctorMods(...$newFieldss) extends ..$bases { ..$body }"
    }

    override def createCustomExpr(classDecl: ClassDef, compDeclOpt: Option[ModuleDef] = None): Any = {
      val tmpClassDefTree = appendClassBody(classDecl, classInfo => List(getNoArgsContrWithCurrying(classInfo.classParamss, classInfo.body)))
      val rest = getClassWithBeanProperty(tmpClassDefTree)
      c.Expr(
        q"""
          ${compDeclOpt.fold(EmptyTree)(x => x)}
          $rest
         """)
    }

    override def checkAnnottees(annottees: Seq[c.universe.Expr[Any]]): Unit = {
      super.checkAnnottees(annottees)
      val annotateeClass: ClassDef = checkGetClassDef(annottees)
      if (!isCaseClass(annotateeClass)) {
        c.abort(c.enclosingPosition, ErrorMessage.ONLY_CASE_CLASS)
      }
    }
  }

}
