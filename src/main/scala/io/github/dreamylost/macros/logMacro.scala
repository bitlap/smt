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

import io.github.dreamylost.ROOT_PACKAGE
import io.github.dreamylost.logs.LogType
import io.github.dreamylost.logs.LogType._

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

    override def impl(annottees: c.universe.Expr[Any]*): c.universe.Expr[Any] = {
      def getLogType(logType: Tree): LogType = {
        if (logType.children.exists(t => t.toString().contains(ROOT_PACKAGE))) {
          evalTree(logType.asInstanceOf[Tree]) // TODO remove asInstanceOf
        } else {
          LogType.getLogType(logType.toString())
        }
      }
      val args: (Boolean, LogType) = extractArgumentsTuple2 {
        case q"new log(logType=$logType)" =>
          val tpe = getLogType(logType.asInstanceOf[Tree])
          (false, tpe)
        case q"new log(verbose=$verbose)" => (evalTree(verbose.asInstanceOf[Tree]), LogType.JLog)
        case q"new log($logType)" =>
          val tpe = getLogType(logType.asInstanceOf[Tree])
          (false, tpe)
        case q"new log(verbose=$verbose, logType=$logType)" =>
          val tpe = getLogType(logType.asInstanceOf[Tree])
          (evalTree(verbose.asInstanceOf[Tree]), tpe)
        case q"new log()" => (false, LogType.JLog)
        case _            => c.abort(c.enclosingPosition, ErrorMessage.UNEXPECTED_PATTERN)
      }

      c.info(c.enclosingPosition, s"annottees: $annottees, args: $args", force = args._1)

      val logTree = annottees.map(_.tree) match {
        // Match a class, and expand, get class/object name.
        case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" :: _ =>
          LogType.getLogImpl(args._2).getTemplate(c)(tpname.asInstanceOf[TypeName].toTermName.decodedName.toString, isClass = true)
        case q"$mods object $tpname extends { ..$earlydefns } with ..$parents { $self => ..$stats }" :: _ =>
          LogType.getLogImpl(args._2).getTemplate(c)(tpname.asInstanceOf[TermName].decodedName.toString, isClass = false)
        case _ => c.abort(c.enclosingPosition, s"Annotation is only supported on class or object.")
      }

      // add result into class
      val resTree = annottees.map(_.tree) match {
        case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" :: _ =>
          q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..${List(logTree) ::: stats.toList} }"
        case q"$mods object $tpname extends { ..$earlydefns } with ..$parents { $self => ..$stats }" :: _ =>
          q"$mods object $tpname extends { ..$earlydefns } with ..$parents { $self => ..${List(logTree) ::: stats.toList} }"
        // Note: If a class is annotated and it has a companion, then both are passed into the macro.
        // (But not vice versa - if an object is annotated and it has a companion class, only the object itself is expanded).
        // see https://docs.scala-lang.org/overviews/macros/annotations.html
      }

      val res = treeResultWithCompanionObject(resTree, annottees: _*)
      printTree(force = args._1, res)
      c.Expr[Any](resTree)
    }
  }
}
