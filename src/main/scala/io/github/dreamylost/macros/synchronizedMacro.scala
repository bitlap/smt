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
object synchronizedMacro {

  class SynchronizedProcessor(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

    import c.universe._

    private val extractArgumentsDetail: (Boolean, String) = extractArgumentsTuple2 {
      case q"new synchronized(verbose=$verbose, lockedName=$lock)" => (evalTree(verbose.asInstanceOf[Tree]), evalTree(lock.asInstanceOf[Tree]))
      case q"new synchronized(lockedName=$lock)" => (false, evalTree(lock.asInstanceOf[Tree]))
      case q"new synchronized()" => (false, "this")
      case _ => c.abort(c.enclosingPosition, ErrorMessage.UNEXPECTED_PATTERN)
    }

    override def impl(annottees: c.universe.Expr[Any]*): c.universe.Expr[Any] = {
      val resTree = annottees.map(_.tree) match {
        case (defDef: DefDef) :: Nil =>
          val body = if (extractArgumentsDetail._2 != null) {
            if (extractArgumentsDetail._2 == "this") {
              q"${This(TypeName(""))}.synchronized { ${defDef.rhs} }"
            } else {
              q"${TermName(extractArgumentsDetail._2)}.synchronized { ${defDef.rhs} }"
            }
          } else {
            c.abort(c.enclosingPosition, "Invalid args, lockName cannot be a null!")
          }
          mapToMethodDef(defDef, body)
        case _ => c.abort(c.enclosingPosition, "Invalid annotation target: not a method")
      }
      printTree(extractArgumentsDetail._1, resTree)
      c.Expr[Any](resTree)
    }
  }

}
