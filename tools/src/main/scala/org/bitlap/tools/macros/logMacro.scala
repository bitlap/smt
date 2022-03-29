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

import org.bitlap.tools.logs.{ LogArgument, LogType }
import org.bitlap.tools.logs
import org.bitlap.tools.logs.LogType._

import scala.reflect.macros.whitebox

/**
 *
 * @author 梦境迷离
 * @since 2021/7/7
 * @version 1.0
 */
object logMacro {

  class LogProcessor(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

    import c.universe._

    private val extractArgs: logs.LogType.Value = c.prefix.tree match {
      // use showRaw print ast
      case Apply(Select(New(Ident(TypeName("log"))), termNames.CONSTRUCTOR), List(NamedArg(Ident(TermName("logType")), args))) =>
        LogType.getLogType(args.toString)
      case Apply(Select(New(Ident(TypeName("log"))), termNames.CONSTRUCTOR), List()) =>
        LogType.JLog
      case _ =>
        //        c.info(c.enclosingPosition, s"${showRaw(other)}", true)
        c.abort(c.enclosingPosition, ErrorMessage.UNEXPECTED_PATTERN)
    }

    private def logTree(annottees: Seq[c.universe.Expr[Any]]): c.universe.Tree = {
      val buildArg = (name: Name) => LogArgument(name.toTermName.decodedName.toString, isClass = true)
      (annottees.map(_.tree) match {
        case (classDef: ClassDef) :: Nil =>
          LogType.getLogImpl(extractArgs).getTemplate(c)(buildArg(classDef.name))
        case (moduleDef: ModuleDef) :: Nil =>
          LogType.getLogImpl(extractArgs).getTemplate(c)(buildArg(moduleDef.name).copy(isClass = false))
        case (classDef: ClassDef) :: (_: ModuleDef) :: Nil =>
          LogType.getLogImpl(extractArgs).getTemplate(c)(buildArg(classDef.name))
        case _ => c.abort(c.enclosingPosition, ErrorMessage.ONLY_OBJECT_CLASS)
      }).asInstanceOf[Tree]
    }

    override def impl(annottees: c.universe.Expr[Any]*): c.universe.Expr[Any] = {
      val resTree = annottees.map(_.tree) match {
        case (classDef: ClassDef) :: _ =>
          if (classDef.mods.hasFlag(Flag.CASE)) {
            c.abort(c.enclosingPosition, ErrorMessage.ONLY_OBJECT_CLASS)
          }
          val newClass = extractArgs match {
            case ScalaLoggingLazy | ScalaLoggingStrict =>
              appendImplDefSuper(checkGetClassDef(annottees), _ => List(logTree(annottees)))
            case _ =>
              prependImplDefBody(checkGetClassDef(annottees), _ => List(logTree(annottees)))
          }
          val moduleDef = getModuleDefOption(annottees)
          q"""
             ${if (moduleDef.isEmpty) EmptyTree else moduleDef.get}
             $newClass
           """
        case (_: ModuleDef) :: _ =>
          extractArgs match {
            case ScalaLoggingLazy | ScalaLoggingStrict => appendImplDefSuper(getModuleDefOption(annottees).get, _ => List(logTree(annottees)))
            case _                                     => prependImplDefBody(getModuleDefOption(annottees).get, _ => List(logTree(annottees)))
          }
        // Note: If a class is annotated and it has a companion, then both are passed into the macro.
        // (But not vice versa - if an object is annotated and it has a companion class, only the object itself is expanded).
        // see https://docs.scala-lang.org/overviews/macros/annotations.html
      }

      printTree(force = true, resTree)
      c.Expr[Any](resTree)
    }
  }

}
