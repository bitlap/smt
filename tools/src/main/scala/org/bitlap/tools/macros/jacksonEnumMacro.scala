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

package org.bitlap.tools.macros

import scala.reflect.macros.whitebox

object jacksonEnumMacro {

  class JacksonEnumProcessor(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

    import c.universe._

    private val extractArgs: Seq[String] = {
      c.prefix.tree match {
        case q"new jacksonEnum(nonTypeRefers=$nonTypeRefers)" => evalTree(nonTypeRefers.asInstanceOf[Tree])
        case q"new jacksonEnum($nonTypeRefers)"               => evalTree(nonTypeRefers.asInstanceOf[Tree])
        case q"new jacksonEnum()"                             => Nil
        case _                                                => c.abort(c.enclosingPosition, ErrorMessage.UNEXPECTED_PATTERN)
      }
    }

    private def getJacksonTypeReferClasses(valDefs: List[ValDef]): Seq[Tree] = {
      val safeValDefs = valDefAccessors(valDefs)
      // Enum ?
      safeValDefs
        .filter(_.symbol.name.toTermName.toString == "Value")
        .map(getTypeTermName)
        .filter(v => !extractArgs.contains(v.decodedName.toString))
        .distinct
        .map(c =>
          q"""class ${TypeName(
            c.decodedName.toString + "TypeRefer"
          )} extends _root_.com.fasterxml.jackson.core.`type`.TypeReference[$c.type]"""
        )
    }

    private def getTypeTermName(valDefTree: Tree): c.universe.TermName = {
      val safeValDef = valDefAccessors(Seq(valDefTree)).head
      getTypeTermName(safeValDef)
    }

    private def getTypeTermName(accessor: ValDefAccessor): c.universe.TermName = {
      val paramTypeStr = accessor.paramType.toString
      TermName(paramTypeStr.split("\\.").last)
    }

    private def getAnnotation(valDefTree: Tree): Tree =
      q"new com.fasterxml.jackson.module.scala.JsonScalaEnumeration(classOf[${TypeName(getTypeTermName(valDefTree).decodedName.toString + "TypeRefer")}])"

    private def replaceAnnotation(valDefTree: Tree): Tree = {
      val safeValDef = valDefAccessors(Seq(valDefTree)).head
      if (safeValDef.typeName.decodedName.toString == "Value") {
        // duplication should be removed
        val mods = safeValDef.mods.mapAnnotations { f =>
          if (
            !f.toString().contains("JsonScalaEnumeration") &&
            !extractArgs.contains(getTypeTermName(safeValDef).decodedName.toString)
          ) f ++ List(getAnnotation(valDefTree))
          else f
        }
        ValDef(mods, safeValDef.name, safeValDef.tpt, safeValDef.rhs)
      } else {
        valDefTree
      }
    }

    override def createCustomExpr(classDecl: c.universe.ClassDef, compDeclOpt: Option[c.universe.ModuleDef]): Any = {
      // return all typeReferClasses and new classDef
      val classDefinition = mapToClassDeclInfo(classDecl)
      val valDefs = classDefinition.classParamss.flatten.map(_.asInstanceOf[ValDef])
      val typeReferClasses = getJacksonTypeReferClasses(valDefs).distinct
      val q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends ..$bases { ..$body }" = classDecl
      val newFieldss = paramss.asInstanceOf[List[List[Tree]]].map(_.map(replaceAnnotation))
      val newClass = q"$mods class $tpname[..$tparams] $ctorMods(...$newFieldss) extends ..$bases { ..$body }"
      val res =
        q"""
           ..$typeReferClasses
             
           $newClass 
         """

      c.Expr(q"""
          ${compDeclOpt.fold(EmptyTree)(x => x)}
          $res
         """)
    }
  }
}
