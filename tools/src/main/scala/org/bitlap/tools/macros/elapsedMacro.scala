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

import scala.concurrent.duration._
import scala.reflect.macros.whitebox

/**
 * 1.Annotation is only supported use on non-abstract method.
 * 2.For methods that are not future, `try finally` is used to implement the timing of the method.
 * 3.For methods that are Futures, `Future map` is used to implement the timing of the method.
 *
 * @author 梦境迷离
 * @since 2021/8/7
 * @version 1.0
 */
object elapsedMacro {

  class ElapsedProcessor(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

    import c.universe._

    private lazy val start: c.universe.TermName = TermName("$elapsedBegin")
    private lazy val valDef: c.universe.TermName = TermName("$elapsed")

    private def getLogLevel(logLevel: Tree): LogLevel = {
      if (logLevel.children.exists(t => t.toString().contains(PACKAGE))) {
        evalTree(logLevel)
      } else {
        LogLevel.getLogLevel(logLevel.toString())
      }
    }

    private val extractArgumentsDetail: (Duration, LogLevel) = extractArgumentsTuple2 {
      case q"new elapsed(limit=$limit, logLevel=$logLevel)" => (evalTree(limit.asInstanceOf[Tree]), getLogLevel(logLevel.asInstanceOf[Tree]))
      case _ => c.abort(c.enclosingPosition, ErrorMessage.UNEXPECTED_PATTERN)
    }

    private def getStartExpr: c.universe.Tree = {
      q"""val $start = _root_.scala.concurrent.duration.Duration.fromNanos(System.nanoTime())"""
    }

    private def getLog(methodName: TermName, logBy: Tree): c.universe.Tree = {
      // CI will fail when use lambda.
      implicit val durationApply: c.universe.Liftable[Duration] = new Liftable[Duration] {
        override def apply(value: Duration): c.universe.Tree = q"${value._1}"
      }
      q"""
        val $valDef = _root_.scala.concurrent.duration.Duration.fromNanos(System.nanoTime()) - $start
        if ($valDef._1 >= ${extractArgumentsDetail._1}) $logBy(StringContext("slow invoked method: [", "] elapsed [", " ms]").s(${methodName.toString}, $valDef.toMillis))
      """
    }

    private def getPrintlnLog(methodName: TermName): c.universe.Tree = {
      val log = findValDefInEnclosingClass(TypeName("org.slf4j.Logger"))
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
      if (defDef.rhs.isEmpty) {
        c.abort(c.enclosingPosition, "Annotation is only supported use on non-abstract method.")
      }

      if (defDef.rhs.children.size < 2) {
        c.abort(c.enclosingPosition, "The method returned directly by an expression is not supported.")
      }
      mapToMethodDef(defDef, defDefMap.apply(defDef))
    }

    private def getNewMethodWithFuture(defDef: DefDef): DefDef = {
      mapToNewMethod(defDef, defDef => {
        q"""
          $getStartExpr
          val resFuture = ${defDef.rhs}
          resFuture.map { res => ..${getPrintlnLog(defDef.name)} ; res }(_root_.scala.concurrent.ExecutionContext.Implicits.global)
        """
      })
    }

    // There may be a half-way exit, rather than the one whose last expression is exit.
    // Unreliable function!!!
    //    private def returnEarly(defDef: DefDef, trees: Tree*): List[Tree] = {
    //      val ifElseMatch = (f: If) => {
    //        if (f.elsep.nonEmpty) {
    //          if (f.elsep.children.nonEmpty && f.elsep.children.size > 1) {
    //            If(f.cond, f.thenp, q"..${returnEarly(defDef, f.elsep.children: _*)}")
    //          } else {
    //            If(f.cond, f.thenp, q"..${returnEarly(defDef, f.elsep)}")
    //          }
    //        } else {
    //          f //no test
    //        }
    //      }
    //      if (trees.isEmpty) return Nil
    //      trees.map {
    //        case r: Return =>
    //          q"""
    //             ..${getPrintlnLog(defDef.name)}
    //             $r
    //            """
    //        case f: If => //support if return
    //          c.info(c.enclosingPosition, s"returnEarly: thenp: ${f.thenp}, children: ${f.thenp.children}, cond: ${f.cond}", force = true)
    //          c.info(c.enclosingPosition, s"returnEarly: elsep: ${f.elsep}, children: ${f.elsep.children}, cond: ${f.cond}", force = true)
    //          if (f.thenp.nonEmpty) {
    //            if (f.thenp.children.nonEmpty && f.thenp.children.size > 1) {
    //              val ifTree = If(f.cond, q"..${returnEarly(defDef, f.thenp.children: _*)}", f.elsep)
    //              ifElseMatch(ifTree)
    //            } else {
    //              val ifTree = If(f.cond, q"..${returnEarly(defDef, f.thenp)}", f.elsep)
    //              ifElseMatch(ifTree)
    //            }
    //          } else {
    //            ifElseMatch(f) //no test
    //          }
    //        case t =>
    //          // TODO support for/while/switch
    //          c.info(c.enclosingPosition, s"returnEarly: not support expr: $t", force = true)
    //          t
    //      }.toList
    //    }

    private def getNewMethod(defDef: DefDef): DefDef = {
      mapToNewMethod(defDef, defDef => {
        q"""
          $getStartExpr
          ${Try(defDef.rhs, Nil, getPrintlnLog(defDef.name))}
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
            getNewMethod(defDef)
          }
      }
      printTree(force = true, resTree)
      c.Expr[Any](resTree)
    }
  }

}
