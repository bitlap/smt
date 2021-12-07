/*
 * Copyright (c) 2021 org.bitlap
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
 * @since 2021/7/7
 * @version 1.0
 */
object toStringMacro {

  private final case class Argument(verbose: Boolean, includeInternalFields: Boolean, includeFieldNames: Boolean, callSuper: Boolean)

  class ToStringProcessor(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

    import c.universe._

    private def extractTree(aa: Tree, bb: Tree, cc: Tree, dd: Tree): (Boolean, Boolean, Boolean, Boolean) = {
      (
        evalTree[Boolean](aa),
        evalTree[Boolean](bb),
        evalTree[Boolean](cc),
        evalTree[Boolean](dd)
      )
    }

    private val extractArgumentsDetail: (Boolean, Boolean, Boolean, Boolean) = extractArgumentsTuple4 {
      case q"new toString(includeInternalFields=$bb, includeFieldNames=$cc, callSuper=$dd)" =>
        extractTree(q"false", bb.asInstanceOf[Tree], cc.asInstanceOf[Tree], dd.asInstanceOf[Tree])
      case q"new toString(verbose=$aa, includeInternalFields=$bb, includeFieldNames=$cc)" =>
        extractTree(aa.asInstanceOf[Tree], bb.asInstanceOf[Tree], cc.asInstanceOf[Tree], q"false")
      case q"new toString($aa, $bb, $cc)" =>
        extractTree(aa.asInstanceOf[Tree], bb.asInstanceOf[Tree], cc.asInstanceOf[Tree], q"false")
      case q"new toString(verbose=$aa, includeInternalFields=$bb, includeFieldNames=$cc, callSuper=$dd)" =>
        extractTree(aa.asInstanceOf[Tree], bb.asInstanceOf[Tree], cc.asInstanceOf[Tree], dd.asInstanceOf[Tree])
      case q"new toString($aa, $bb, $cc, $dd)" =>
        extractTree(aa.asInstanceOf[Tree], bb.asInstanceOf[Tree], cc.asInstanceOf[Tree], dd.asInstanceOf[Tree])
      case q"new toString(includeInternalFields=$bb, includeFieldNames=$cc)" =>
        extractTree(q"false", bb.asInstanceOf[Tree], cc.asInstanceOf[Tree], q"false")
      case q"new toString(includeInternalFields=$bb)" =>
        extractTree(q"false", bb.asInstanceOf[Tree], q"true", q"false")
      case q"new toString(includeFieldNames=$cc)" =>
        extractTree(q"false", q"true", cc.asInstanceOf[Tree], q"false")
      case q"new toString()" => (false, true, true, false)
      case _                 => c.abort(c.enclosingPosition, ErrorMessage.UNEXPECTED_PATTERN)
    }

    override val verbose: Boolean = extractArgumentsDetail._1

    override def createCustomExpr(classDecl: c.universe.ClassDef, compDeclOpt: Option[c.universe.ModuleDef]): Any = {
      // extract parameters of annotation, must in order
      val argument = Argument(
        extractArgumentsDetail._1,
        extractArgumentsDetail._2,
        extractArgumentsDetail._3,
        extractArgumentsDetail._4
      )
      val resTree = appendClassBody(classDecl, _ => List(getToStringTemplate(argument, classDecl)))
      c.Expr(
        q"""
          ${compDeclOpt.fold(EmptyTree)(x => x)}
          $resTree
         """)
    }

    private def printField(argument: Argument, lastParam: Option[String], field: Tree): Tree = {
      // Print one field as <name of the field>+"="+fieldName
      if (argument.includeFieldNames) {
        lastParam.fold(q"$field") { lp =>
          field match {
            case v: ValDef =>
              if (v.name.toTermName.decodedName.toString != lp) q"""${v.name.toTermName.decodedName.toString}+${"="}+this.${v.name}+${", "}"""
              else q"""${v.name.toTermName.decodedName.toString}+${"="}+this.${v.name}"""
            case _ => q"$field"
          }
        }
      } else {
        lastParam.fold(q"$field") { lp =>
          field match {
            case v: ValDef => if (v.name.toTermName.decodedName.toString != lp) q"""${v.name}+${", "}""" else q"""${v.name}"""
            case _         => if (field.toString() != lp) q"""$field+${", "}""" else q"""$field"""
          }
        }
      }
    }

    private def getToStringTemplate(argument: Argument, classDecl: ClassDef): Tree = {
      // For a given class definition, separate the components of the class
      val classDefinition = mapToClassDeclInfo(classDecl)
      // Check the type of the class, whether it already contains its own toString
      val annotteeClassFieldDefinitions = classDefinition.body.filter(_ match {
        case _: ValDef => true
        case mem: MemberDef =>
          if (mem.name.decodedName.toString.startsWith("toString")) { // TODO better way
            c.abort(mem.pos, "'toString' method has already defined, please remove it or not use'@toString'")
          }
          false
        case _ => false
      })

      val ctorParams = classDefinition.classParamss.flatten
      val member = if (argument.includeInternalFields) ctorParams ++ annotteeClassFieldDefinitions else ctorParams

      val lastParam = member.lastOption.map {
        case v: ValDef => v.name.toTermName.decodedName.toString
        case c         => c.toString
      }
      val paramsWithName = member.foldLeft(q"${""}")((res, acc) => q"$res + ${printField(argument, lastParam, acc)}")
      //scala/bug https://github.com/scala/bug/issues/3967 not be 'Foo(i=1,j=2)' in standard library
      val toString = q"""override def toString: String = ${classDefinition.className.toTermName.decodedName.toString} + ${"("} + $paramsWithName + ${")"}"""

      // Have super class ?
      if (argument.callSuper && classDefinition.superClasses.nonEmpty) {
        val superClassDef = classDefinition.superClasses.head match {
          case tree: Tree => Some(tree) // TODO type check better
          case _          => None
        }
        superClassDef.fold(toString)(_ => {
          val superClass = q"${"super="}"
          q"override def toString: String = StringContext(${classDefinition.className.toTermName.decodedName.toString} + ${"("} + $superClass, ${if (member.nonEmpty) ", " else ""}+$paramsWithName + ${")"}).s(super.toString)"
        }
        )
      } else {
        toString
      }
    }
  }

}
