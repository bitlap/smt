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

object jacksonEnumMacro {

  class JacksonEnumProcessor(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

    import c.universe._

    private val extractArgumentsDetail: Tuple2[Boolean, Seq[String]] = {
      extractArgumentsTuple2 {
        case q"new jacksonEnum(verbose=$verbose, nonTypeRefers=$nonTypeRefers)" => Tuple2(evalTree(verbose.asInstanceOf[Tree]), evalTree(nonTypeRefers.asInstanceOf[Tree]))
        case q"new jacksonEnum(nonTypeRefers=$nonTypeRefers)" => Tuple2(false, evalTree(nonTypeRefers.asInstanceOf[Tree]))
        case q"new jacksonEnum()" => Tuple2(false, Nil)
        case _ => c.abort(c.enclosingPosition, ErrorMessage.UNEXPECTED_PATTERN)
      }
    }

    private def getJacksonTypeReferClasses(valDefs: List[ValDef]): Seq[Tree] = {
      val safeValDefs = accessors(valDefs)
      // Enum ?
      safeValDefs.filter(_.symbol.name.toTermName.toString == "Value").
        map(getTypeTermName).
        filter(v => !extractArgumentsDetail._2.contains(v.decodedName.toString)).
        distinct.
        map(c => q"""class ${TypeName(c.decodedName.toString + "TypeRefer")} extends _root_.com.fasterxml.jackson.core.`type`.TypeReference[$c.type]""")
    }

    private def getTypeTermName(valDefTree: Tree): c.universe.TermName = {
      val safeValDef = accessors(Seq(valDefTree.asInstanceOf[ValDef])).head
      getTypeTermName(safeValDef)
    }

    private def getTypeTermName(accessor: Accessor): c.universe.TermName = {
      val paramTypeStr = accessor.paramType.toString
      TermName(paramTypeStr.split("\\.").last)
    }

    private def getAnnotation(valDefTree: Tree): Tree = {
      q"new com.fasterxml.jackson.module.scala.JsonScalaEnumeration(classOf[${TypeName(getTypeTermName(valDefTree).decodedName.toString + "TypeRefer")}])"
    }

    private def replaceAnnotation(valDefTree: Tree): Tree = {
      val safeValDef = accessors(Seq(valDefTree.asInstanceOf[ValDef])).head
      if (safeValDef.symbol.name.toTermName.toString == "Value") {
        // duplication should be removed
        val mods = safeValDef.mods.mapAnnotations(f => {
          if (!f.toString().contains("JsonScalaEnumeration") &&
            !extractArgumentsDetail._2.contains(getTypeTermName(safeValDef).decodedName.toString)) f ++ List(getAnnotation(valDefTree)) else f
        })
        if (safeValDef.mods.hasFlag(Flag.MUTABLE)) {
          q"$mods var ${safeValDef.name}: ${safeValDef.paramType} = ${safeValDef.rhs}"
        } else {
          q"$mods val ${safeValDef.name}: ${safeValDef.paramType} = ${safeValDef.rhs}"
        }
      } else {
        valDefTree
      }
    }

    override def impl(annottees: c.universe.Expr[Any]*): c.universe.Expr[Any] = {
      // get class
      val classDef = checkAndGetClassDef(annottees: _*)
      // return all typeReferClasses and new classDef
      val resTree = classDef match {
        case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends ..$bases { ..$body }" =>
          val valDefs = paramss.asInstanceOf[List[List[Tree]]].flatten.map(_.asInstanceOf[ValDef])
          val typeReferClasses = getJacksonTypeReferClasses(valDefs).distinct
          val newClass = modifiedDeclaration(classDef, None).asInstanceOf[Expr[Nothing]]
          q"""
              ..$typeReferClasses
              
               $newClass // get field after replacing annotation for each field in constructor
           """
        case _ => c.abort(c.enclosingPosition, ErrorMessage.ONLY_CLASS)
      }
      printTree(force = extractArgumentsDetail._1, resTree)
      c.Expr(resTree)
    }

    override def modifiedDeclaration(classDecl: c.universe.ClassDef, compDeclOpt: Option[c.universe.ModuleDef]): Any = {
      val newClass = classDecl match {
        case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends ..$bases { ..$body }" =>
          val fieldss = paramss.asInstanceOf[List[List[Tree]]]
          q"""
             $mods class $tpname[..$tparams] $ctorMods(...${fieldss.map(_.map(replaceAnnotation))}) extends ..$bases { ..$body }
           """
        case _ => c.abort(c.enclosingPosition, ErrorMessage.ONLY_CLASS)
      }
      c.Expr(newClass)
    }
  }
}

