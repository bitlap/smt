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

import org.bitlap.tools.LogLevel.LogLevel
import org.bitlap.tools.{ LogLevel, PACKAGE }

import scala.reflect.macros.whitebox

/**
 *    1. This annotation only support use on non-abstract method.
 *
 *  2. For methods that are not future, `try finally` is used to implement the timing of the method.
 *
 *  3. For methods that are Futures, `Future onComplete` is used to implement the timing of the method.
 *
 *  @author
 *    梦境迷离
 *  @since 2021/8/7
 *  @version 1.0
 */
object elapsedMacro {

  class ElapsedProcessor(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

    import c.universe._

    private lazy val start: c.universe.TermName  = TermName("$elapsedBegin")
    private lazy val valDef: c.universe.TermName = TermName("$elapsed")

    private def getLogLevel(logLevel: Tree): LogLevel =
      if (logLevel.children.exists(t => t.toString().contains(PACKAGE))) {
        evalTree(logLevel)
      } else {
        LogLevel.getLogLevel(logLevel.toString())
      }

    private val extractOptions: (Int, LogLevel) = c.prefix.tree match {
      case q"new elapsed(limit=$limit, logLevel=$logLevel)" =>
        (evalTree(limit.asInstanceOf[Tree]), getLogLevel(logLevel.asInstanceOf[Tree]))
      case _ => c.abort(c.enclosingPosition, ErrorMessage.UNEXPECTED_PATTERN)
    }

    private def getStartExpr: c.universe.Tree =
      q"""val $start = System.currentTimeMillis()"""

    private def getLog(classNameAndMethodName: String, logBy: Tree): c.universe.Tree =
      q"""
        val $valDef = System.currentTimeMillis() - $start
        if ($valDef >= ${extractOptions._1}) $logBy(StringContext("slow invoked method: [", "] elapsed [", " ms]").s($classNameAndMethodName, $valDef))
      """

    private def getPrintlnLog(classNameAndMethodName: String): c.universe.Tree = {
      val log = findValDefInEnclosingClass(TypeName("org.slf4j.Logger"))
      if (log.isEmpty) { // if there is no slf4j log, print it to the console
        getLog(classNameAndMethodName, q"_root_.scala.Predef.println")
      } else {
        extractOptions._2 match {
          case LogLevel.INFO  => getLog(classNameAndMethodName, q"${log.get}.info")
          case LogLevel.DEBUG => getLog(classNameAndMethodName, q"${log.get}.debug")
          case LogLevel.WARN  => getLog(classNameAndMethodName, q"${log.get}.warn")
        }
      }
    }

    private def mapToNewMethod(defDef: DefDef, defDefMap: DefDef => Tree): c.universe.DefDef = {
      if (defDef.rhs.isEmpty) {
        c.abort(c.enclosingPosition, "Annotation is only supported use on non-abstract method.")
      }

      if (defDef.rhs.children.size < 2) {
        c.abort(c.enclosingPosition, "The method returned directly by an expression is not supported.")
      }
      mapToMethodDef(defDef, defDefMap.apply(defDef))
    }

    private def getNewMethodWithFuture(defDef: DefDef): DefDef =
      // scalafmt: { maxColumn = 400 }
      mapToNewMethod(
        defDef,
        defDef => q"""
          $getStartExpr
          val resFuture = ${defDef.rhs}
          resFuture.onComplete { case _ => ..${getPrintlnLog(getIdentNam(defDef.name))} }(_root_.scala.concurrent.ExecutionContext.Implicits.global)
          resFuture
        """
      )

    private def getIdentNam(method: Name): String =
      s"${c.enclosingClass match {
          case ClassDef(_, name, _, Template(_, _, _)) => name
          case ModuleDef(_, name, Template(_, _, _))   => name
        }}#${method.decodedName.toString}"

    private def getNewMethod(defDef: DefDef): DefDef =
      mapToNewMethod(
        defDef,
        defDef => q"""
          $getStartExpr
          ${Try(defDef.rhs, Nil, getPrintlnLog(getIdentNam(defDef.name)))}
        """
      )

    override def impl(annottees: c.universe.Expr[Any]*): c.universe.Expr[Any] = {
      val resTree = annottees.map(_.tree) match {
        case (defDef @ DefDef(_, _, _, _, tpt, _)) :: Nil =>
          if (tpt.isEmpty) {
            c.abort(c.enclosingPosition, "The return type of the method is not specified!")
          }
          val tp = c.typecheck(tq"$tpt", c.TYPEmode).tpe
          if (tp <:< typeOf[scala.concurrent.Future[_]]) {
            getNewMethodWithFuture(defDef)
          } else {
            getNewMethod(defDef)
          }
      }
      printTree(force = false, resTree)
      c.Expr[Any](resTree)
    }
  }

}
