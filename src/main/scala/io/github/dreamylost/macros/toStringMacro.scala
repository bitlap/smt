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
object toStringMacro extends MacroCommon {

  private final case class Argument(verbose: Boolean, includeInternalFields: Boolean, includeFieldNames: Boolean, callSuper: Boolean)

  def printField(c: whitebox.Context)(argument: Argument, lastParam: Option[String], field: c.universe.Tree): c.universe.Tree = {
    import c.universe._
    // Print one field as <name of the field>+"="+fieldName
    if (argument.includeFieldNames) {
      lastParam.fold(q"$field") { lp =>
        field match {
          case q"$mods var $tname: $tpt = $expr" =>
            if (tname.toString() != lp) q"""${tname.toString()}+${"="}+this.$tname+${", "}""" else q"""${tname.toString()}+${"="}+this.$tname"""
          case q"$mods val $tname: $tpt = $expr" =>
            if (tname.toString() != lp) q"""${tname.toString()}+${"="}+this.$tname+${", "}""" else q"""${tname.toString()}+${"="}+this.$tname"""
          case _ => q"$field"
        }
      }
    } else {
      lastParam.fold(q"$field") { lp =>
        field match {
          case q"$mods var $tname: $tpt = $expr" => if (tname.toString() != lp) q"""$tname+${", "}""" else q"""$tname"""
          case q"$mods val $tname: $tpt = $expr" => if (tname.toString() != lp) q"""$tname+${", "}""" else q"""$tname"""
          case _                                 => if (field.toString() != lp) q"""$field+${", "}""" else q"""$field"""
        }
      }

    }
  }

  private def toStringTemplateImpl(c: whitebox.Context)(argument: Argument, annotateeClass: c.universe.ClassDef): c.universe.Tree = {
    import c.universe._
    // For a given class definition, separate the components of the class
    val (className, annotteeClassParams, superClasses, annotteeClassDefinitions) = {
      annotateeClass match {
        case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" =>
          c.info(c.enclosingPosition, s"parents: $parents", force = argument.verbose)
          (tpname, paramss, parents, stats)
      }
    }
    // Check the type of the class, whether it already contains its own toString
    val annotteeClassFieldDefinitions = annotteeClassDefinitions.asInstanceOf[List[Tree]].filter(p => p match {
      case _: ValDef => true
      case mem: MemberDef =>
        c.info(c.enclosingPosition, s"MemberDef:  ${mem.toString}", force = argument.verbose)
        if (mem.toString().startsWith("override def toString")) { // TODO better way
          c.abort(mem.pos, "'toString' method has already defined, please remove it or not use'@toString'")
        }
        false
      case _ => false
    })

    // For the parameters of a given constructor, separate the parameter components and extract the constructor parameters containing val and var
    val ctorParams = annotteeClassParams.asInstanceOf[List[List[Tree]]].flatten.map {
      case tree @ q"$mods val $tname: $tpt = $expr" => tree
      case tree @ q"$mods var $tname: $tpt = $expr" => tree
    }
    c.info(c.enclosingPosition, s"className： $className, ctorParams: ${ctorParams.toString()}, superClasses: $superClasses", force = argument.verbose)
    c.info(c.enclosingPosition, s"className： $className, fields: ${annotteeClassFieldDefinitions.toString()}", force = argument.verbose)
    val member = if (argument.includeInternalFields) ctorParams ++ annotteeClassFieldDefinitions else ctorParams

    val lastParam = member.lastOption.map {
      case v: ValDef => v.name.toTermName.decodedName.toString
      case c         => c.toString
    }
    val paramsWithName = member.foldLeft(q"${""}")((res, acc) => q"$res + ${printField(c)(argument, lastParam, acc)}")
    //scala/bug https://github.com/scala/bug/issues/3967 not be 'Foo(i=1,j=2)' in standard library
    val toString = q"""override def toString: String = ${className.toString()} + ${"("} + $paramsWithName + ${")"}"""

    // Have super class ?
    if (argument.callSuper && superClasses.nonEmpty) {
      val superClassDef = superClasses.head match {
        case tree: Tree => Some(tree) // TODO type check better
        case _          => None
      }
      superClassDef.fold(toString)(_ => {
        val superClass = q"${"super="}"
        c.info(c.enclosingPosition, s"member: $member, superClass： $superClass, superClassDef: $superClassDef, paramsWithName: $paramsWithName", force = argument.verbose)
        q"override def toString: String = StringContext(${className.toString()} + ${"("} + $superClass, ${if (member.nonEmpty) ", " else ""}+$paramsWithName + ${")"}).s(super.toString)"
      }
      )
    } else {
      toString
    }

  }

  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    // extract parameters of annotation, must in order
    val arg: (Boolean, Boolean, Boolean, Boolean) = extractArgumentsTuple4(c) {
      case q"new toString(includeInternalFields=$bb, includeFieldNames=$cc, callSuper=$dd)" =>
        (false, evalTree(c)(bb.asInstanceOf[Tree]), evalTree(c)(cc.asInstanceOf[Tree]), evalTree(c)(dd.asInstanceOf[Tree]))
      case q"new toString($aa, $bb, $cc)" =>
        (evalTree(c)(aa.asInstanceOf[Tree]), evalTree(c)(bb.asInstanceOf[Tree]), evalTree(c)(cc.asInstanceOf[Tree]), false)

      case q"new toString(verbose=$aa, includeInternalFields=$bb, includeFieldNames=$cc, callSuper=$dd)" =>
        (evalTree(c)(aa.asInstanceOf[Tree]), evalTree(c)(bb.asInstanceOf[Tree]), evalTree(c)(cc.asInstanceOf[Tree]), evalTree(c)(dd.asInstanceOf[Tree]))
      case q"new toString(verbose=$aa, includeInternalFields=$bb, includeFieldNames=$cc)" =>
        (evalTree(c)(aa.asInstanceOf[Tree]), evalTree(c)(bb.asInstanceOf[Tree]), evalTree(c)(cc.asInstanceOf[Tree]), false)
      case q"new toString($aa, $bb, $cc, $dd)" =>
        (evalTree(c)(aa.asInstanceOf[Tree]), evalTree(c)(bb.asInstanceOf[Tree]), evalTree(c)(cc.asInstanceOf[Tree]), evalTree(c)(dd.asInstanceOf[Tree]))

      case q"new toString(includeInternalFields=$bb, includeFieldNames=$cc)" =>
        (false, evalTree(c)(bb.asInstanceOf[Tree]), evalTree(c)(cc.asInstanceOf[Tree]), false)
      case q"new toString(includeInternalFields=$bb)" =>
        (false, evalTree(c)(bb.asInstanceOf[Tree]), true, false)
      case q"new toString(includeFieldNames=$cc)" =>
        (false, true, evalTree(c)(cc.asInstanceOf[Tree]), false)
      case q"new toString()" => (false, true, true, false)
      case _                 => c.abort(c.enclosingPosition, ErrorMessage.UNEXPECTED_PATTERN)
    }
    val argument = Argument(arg._1, arg._2, arg._3, arg._4)
    c.info(c.enclosingPosition, s"toString annottees: $annottees", force = argument.verbose)
    // Check the type of the class, which can only be defined on the ordinary class
    val annotateeClass: ClassDef = checkAndGetClassDef(c)(annottees: _*)
    val isCase: Boolean = isCaseClass(c)(annotateeClass)

    c.info(c.enclosingPosition, s"impl argument: $argument, isCase: $isCase", force = argument.verbose)
    val resMethod = toStringTemplateImpl(c)(argument, annotateeClass)
    val resTree = annotateeClass match {
      case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" =>
        q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..${stats.toList.:+(resMethod)} }"
    }

    val res = treeResultWithCompanionObject(c)(resTree, annottees: _*)
    printTree(c)(argument.verbose, res)
    c.Expr[Any](res)
  }
}
