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

import io.github.dreamylost.LogLevel.LogLevel
import io.github.dreamylost.{ LogLevel, PACKAGE }

import scala.concurrent.duration._
import scala.reflect.macros.whitebox

/**
 * 1.The last expression is used as the return value, so time-consuming operations should not be called directly on the last return.
 * 2.Not support when method body only has one expr
 *
 * @author 梦境迷离
 * @since 2021/8/7
 * @version 1.0
 */
object elapsedMacro {

  class ElapsedProcessor(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

    import c.universe._

    private lazy val start: c.universe.TermName = TermName("$elapsedBegin")
    private lazy val valDef = TermName("$elapsed")

    private def getLogLevel(logLevel: Tree): LogLevel = {
      if (logLevel.children.exists(t => t.toString().contains(PACKAGE))) {
        evalTree(logLevel)
      } else {
        LogLevel.getLogLevel(logLevel.toString())
      }
    }

    private val extractArgumentsDetail: (Duration, LogLevel) = extractArgumentsTuple2 {
      case q"new elapsed(limit=$limit, logLevel=$logLevel)" => (evalTree[Duration](limit.asInstanceOf[Tree]), getLogLevel(logLevel.asInstanceOf[Tree]))
      case _ => c.abort(c.enclosingPosition, ErrorMessage.UNEXPECTED_PATTERN)
    }

    private def getStartExpr: c.universe.Tree = {
      q"""val $start = _root_.scala.concurrent.duration.Duration.fromNanos(System.nanoTime())"""
    }

    private def getLog(methodName: TermName, logBy: Tree): c.universe.Tree = {
      implicit val durationApply: c.universe.Liftable[Duration] = new Liftable[Duration] {
        override def apply(value: Duration): c.universe.Tree = q"${value._1}"
      }
      q"""
          val $valDef = _root_.scala.concurrent.duration.Duration.fromNanos(System.nanoTime()) - $start
          if ($valDef._1 >= ${extractArgumentsDetail._1}) $logBy(StringContext("slow invoked: [", "] elapsed [", "]").s(${methodName.toString}, $valDef.toMillis))
      """
    }

    private def getPrintlnLog(methodName: TermName): c.universe.Tree = {
      val log = findNameOnEnclosingClass(TypeName("org.slf4j.Logger"))
      if (log.isEmpty) { // if there is no slf4j log, print it to the console
        getLog(methodName, q"_root_.scala.Predef.println")
      } else {
        extractArgumentsDetail._2 match {
          case LogLevel.INFO  => getLog(methodName, q"${log.get}.info")
          case LogLevel.DEBUG => getLog(methodName, q"${log.get}.debug")
          case LogLevel.WARN  => getLog(methodName, q"${log.get}.warn")
        }
      }
    }

    private def mapToNewMethod(defDef: DefDef, defDefMap: DefDef => Tree): c.universe.DefDef = {
      val rhsMembers = if (!defDef.rhs.isEmpty) {
        defDef.rhs.children
      } else {
        Nil
      }
      if (rhsMembers.nonEmpty) {
        mapToMethodDef(defDef, defDefMap.apply(defDef))
      } else {
        defDef
      }
    }

    private def getNewMethodWithFuture(defDef: DefDef): DefDef = {
      mapToNewMethod(defDef, defDef => {
        q"""
          $getStartExpr
          val resFuture = ${defDef.rhs}
          resFuture. map { res => ..${getPrintlnLog(defDef.name)} ; res }(_root_.scala.concurrent.ExecutionContext.Implicits.global)
        """
      })
    }

    // There may be a half-way exit, rather than the one whose last expression is exit.
    private def returnEarly(defDef: DefDef, trees: List[Tree]): List[Tree] = {
      if (trees.isEmpty) return Nil
      trees.map {
        case r: Return =>
          q"""
             ..${getPrintlnLog(defDef.name)}
             $r
            """
        case f: If => //support if return
          if (!f.thenp.isEmpty) {
            If(f.cond, q"..${returnEarly(defDef, List(f.thenp))}", f.elsep)
          } else if (!f.elsep.isEmpty) {
            If(f.cond, f.thenp, q"..${returnEarly(defDef, List(f.elsep))}")
          } else {
            f
          }
        case t => t
        // TODO support for/while/switch
      }
    }

    private def getNewMethod(defDef: DefDef): DefDef = {
      mapToNewMethod(defDef, defDef => {
        val heads = defDef.rhs.children.init
        val last = defDef.rhs.children.last
        q"""
          $getStartExpr
          ..${returnEarly(defDef, heads)}
          ..${getPrintlnLog(defDef.name)}
          $last
        """
      })
    }

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
            if (defDef.rhs.nonEmpty && defDef.rhs.children.size < 2) {
              c.abort(c.enclosingPosition, "The method returned directly by an expression is not supported.")
            }
            getNewMethod(defDef)
          }
      }
      printTree(force = true, resTree)
      c.Expr[Any](resTree)
    }
  }

}
