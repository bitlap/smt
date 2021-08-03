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

import scala.collection.mutable
import scala.reflect.macros.whitebox

object jsonEnumMacro {

  class JsonEnumProcessor(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

    import c.universe._

    private def jsonEnumFormatter(fields: List[Tree]): (List[Tree], List[Tree]) = {
      val enumTypeRefers = mutable.ArrayBuffer.empty[Tree]
      fields.length match {
        case 0 => c.abort(c.enclosingPosition, "Cannot create jsonEnum formatter for case class with no fields")
        case _ =>
          val newParamss = fields.map { field =>
            field match {
              case v: ValDef if v.mods.annotations.contains("@jsonEnum") =>
                c.info(c.enclosingPosition, v.mods.annotations.toString(), true)
                enumTypeRefers += q"class EnumTypeTypeRefer extends TypeReference[${v.tpt}.type]"

                q"@JsonScalaEnumeration(classOf[${v.tpt}] ${v.name}: ${v.tpt})"
              case tree => tree
            }
          }
          (enumTypeRefers.toList, newParamss)
      }
    }

    override def impl(annottees: c.universe.Expr[Any]*): c.universe.Expr[Any] = {
      val resTree = handleWithImplType(annottees: _*)(modifiedDeclaration)
      printTree(force = true, resTree.tree)
      resTree
    }

    override def modifiedDeclaration(classDecl: c.universe.ClassDef, compDeclOpt: Option[c.universe.ModuleDef]): Any = {
      val (className, fields) = classDecl match {
        case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$body}" =>
          if (!mods.asInstanceOf[Modifiers].hasFlag(Flag.CASE)) {
            c.abort(c.enclosingPosition, s"Annotation is only supported on case class. classDef: $classDecl, mods: $mods")
          } else {
            c.info(c.enclosingPosition, s"modifiedDeclaration className: $tpname, paramss: $paramss", force = true)
            //            val fields = paramss.asInstanceOf[List[List[Tree]]]
            //            fields.filter()
            (tpname.asInstanceOf[TypeName], paramss.asInstanceOf[List[List[Tree]]])
          }
        case _ => c.abort(c.enclosingPosition, s"${ErrorMessage.ONLY_CLASS}, classDef: $classDecl")
      }

      val (enumTypeRefers, newParamss) = jsonEnumFormatter(fields.flatten)
      val o = q"${enumTypeRefers.mkString("\n")}"
      val compDecl = modifiedCompanion(compDeclOpt, o, className)
      val newClassDecl = classDecl match {
        case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$body}" =>
          q"$mods class $tpname[..$tparams] $ctorMods(...$newParamss) extends { ..$body}"
        case _ => c.abort(c.enclosingPosition, s"Annotation can not support. classDef: $classDecl")
      }
      c.Expr(
        q"""
           $newClassDecl
           $compDecl
         """
      )
    }
  }
}

