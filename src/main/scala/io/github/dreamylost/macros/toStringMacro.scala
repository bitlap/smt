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
object toStringMacro {

  private final case class Argument(verbose: Boolean, includeInternalFields: Boolean, includeFieldNames: Boolean, callSuper: Boolean)

  class ToStringProcessor(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

    import c.universe._

    private val extractArgumentsDetail = extractArgumentsTuple4 {
      case q"new toString(includeInternalFields=$bb, includeFieldNames=$cc, callSuper=$dd)" =>
        (false, evalTree(bb.asInstanceOf[Tree]), evalTree(cc.asInstanceOf[Tree]), evalTree(dd.asInstanceOf[Tree]))
      case q"new toString($aa, $bb, $cc)" =>
        (evalTree(aa.asInstanceOf[Tree]), evalTree(bb.asInstanceOf[Tree]), evalTree(cc.asInstanceOf[Tree]), false)
      case q"new toString(verbose=$aa, includeInternalFields=$bb, includeFieldNames=$cc, callSuper=$dd)" =>
        (evalTree(aa.asInstanceOf[Tree]), evalTree(bb.asInstanceOf[Tree]), evalTree(cc.asInstanceOf[Tree]), evalTree(dd.asInstanceOf[Tree]))
      case q"new toString(verbose=$aa, includeInternalFields=$bb, includeFieldNames=$cc)" =>
        (evalTree(aa.asInstanceOf[Tree]), evalTree(bb.asInstanceOf[Tree]), evalTree(cc.asInstanceOf[Tree]), false)
      case q"new toString($aa, $bb, $cc, $dd)" =>
        (evalTree(aa.asInstanceOf[Tree]), evalTree(bb.asInstanceOf[Tree]), evalTree(cc.asInstanceOf[Tree]), evalTree(dd.asInstanceOf[Tree]))
      case q"new toString(includeInternalFields=$bb, includeFieldNames=$cc)" =>
        (false, evalTree(bb.asInstanceOf[Tree]), evalTree(cc.asInstanceOf[Tree]), false)
      case q"new toString(includeInternalFields=$bb)" =>
        (false, evalTree(bb.asInstanceOf[Tree]), true, false)
      case q"new toString(includeFieldNames=$cc)" =>
        (false, true, evalTree(cc.asInstanceOf[Tree]), false)
      case q"new toString()" => (false, true, true, false)
      case _                 => c.abort(c.enclosingPosition, ErrorMessage.UNEXPECTED_PATTERN)
    }

    override def impl(annottees: c.universe.Expr[Any]*): c.universe.Expr[Any] = {
      // extract parameters of annotation, must in order
      val argument = Argument(extractArgumentsDetail._1, extractArgumentsDetail._2, extractArgumentsDetail._3, extractArgumentsDetail._4)
      // Check the type of the class, which can only be defined on the ordinary class
      val annotateeClass: ClassDef = checkAndGetClassDef(annottees: _*)
      val isCase: Boolean = isCaseClass(annotateeClass)
      val resMethod = toStringTemplateImpl(argument, annotateeClass)
      val resTree = annotateeClass match {
        case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" =>
          q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..${stats.toList.:+(resMethod)} }"
      }

      val res = treeResultWithCompanionObject(resTree, annottees: _*)
      printTree(argument.verbose, res)
      c.Expr[Any](res)
    }

    private def printField(argument: Argument, lastParam: Option[String], field: Tree): Tree = {
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

    private def toStringTemplateImpl(argument: Argument, annotateeClass: ClassDef): Tree = {
      // For a given class definition, separate the components of the class
      val (className, annotteeClassParams, superClasses, annotteeClassDefinitions) = {
        annotateeClass match {
          case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" =>
            c.info(c.enclosingPosition, s"parents: $parents", force = argument.verbose)
            (tpname.asInstanceOf[TypeName], paramss.asInstanceOf[List[List[Tree]]], parents, stats.asInstanceOf[List[Tree]])
        }
      }
      // Check the type of the class, whether it already contains its own toString
      val annotteeClassFieldDefinitions = annotteeClassDefinitions.filter(p => p match {
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
      val ctorParams = annotteeClassParams.flatten.map {
        case tree @ q"$mods val $tname: $tpt = $expr" => tree
        case tree @ q"$mods var $tname: $tpt = $expr" => tree
      }
      val member = if (argument.includeInternalFields) ctorParams ++ annotteeClassFieldDefinitions else ctorParams

      val lastParam = member.lastOption.map {
        case v: ValDef => v.name.toTermName.decodedName.toString
        case c         => c.toString
      }
      val paramsWithName = member.foldLeft(q"${""}")((res, acc) => q"$res + ${printField(argument, lastParam, acc)}")
      //scala/bug https://github.com/scala/bug/issues/3967 not be 'Foo(i=1,j=2)' in standard library
      val toString = q"""override def toString: String = ${className.toTermName.decodedName.toString} + ${"("} + $paramsWithName + ${")"}"""

      // Have super class ?
      if (argument.callSuper && superClasses.nonEmpty) {
        val superClassDef = superClasses.head match {
          case tree: Tree => Some(tree) // TODO type check better
          case _          => None
        }
        superClassDef.fold(toString)(_ => {
          val superClass = q"${"super="}"
          c.info(c.enclosingPosition, s"member: $member, superClass： $superClass, superClassDef: $superClassDef, paramsWithName: $paramsWithName", force = argument.verbose)
          q"override def toString: String = StringContext(${className.toTermName.decodedName.toString} + ${"("} + $superClass, ${if (member.nonEmpty) ", " else ""}+$paramsWithName + ${")"}).s(super.toString)"
        }
        )
      } else {
        toString
      }
    }
  }

}
