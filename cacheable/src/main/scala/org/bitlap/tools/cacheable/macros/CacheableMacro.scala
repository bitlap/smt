/*
 * Copyright (c) 2022 org.bitlap
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

package org.bitlap.tools.cacheable.macros

import scala.reflect.macros.whitebox

/**
 * GetAndSet cache
 *
 * @author 梦境迷离
 * @since 2022/3/18
 * @version 1.0
 */
object CacheableMacro {

  class CacheableProcessor(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

    import c.universe._

    private lazy val resultValName: c.universe.TermName = TermName("$result")
    private lazy val keyValName: c.universe.TermName = TermName("$key")

    override protected val verbose: Boolean = {
      c.prefix.tree match {
        case q"new cacheable(verbose=$verbose)" => c.eval(c.Expr[Boolean](c.untypecheck(verbose.asInstanceOf[Tree].duplicate)))
        case q"new cacheable($verbose)"         => c.eval(c.Expr[Boolean](c.untypecheck(verbose.asInstanceOf[Tree].duplicate)))
        case q"new cacheable()"                 => false
      }
    }

    private def getParamsName(vparamss: List[List[ValDef]]): List[List[TermName]] = {
      vparamss.map(_.map(_.name))
    }

    def impl(annottees: c.universe.Expr[Any]*): c.universe.Expr[Any] = {
      val resTree = annottees.map(_.tree) match {
        case (defDef @ DefDef(mods, name, tparams, vparamss, tpt, _)) :: Nil =>
          if (tpt.isEmpty) {
            c.abort(c.enclosingPosition, "The return type of the method is not specified!")
          }
          val tp = c.typecheck(tq"$tpt", c.TYPEmode).tpe
          if (!(tp <:< typeOf[zio.ZIO[_, _, _]]) && !(tp <:< typeOf[zio.stream.ZStream[_, _, _]])) {
            c.abort(c.enclosingPosition, s"The return type of the method not support type: `${tp.typeSymbol.name.toString}`!")
          }
          val newBody =
            q"""
             val $resultValName = ${defDef.rhs}
             val $keyValName = List(${getEnclosingClassName}, ${name.decodedName.toString})
             org.bitlap.tools.cacheable.Cache($resultValName)($keyValName, ..${getParamsName(vparamss)})
           """
          DefDef(mods, name, tparams, vparamss, tpt, newBody)

      }
      printTree(force = verbose, resTree)
      c.Expr[Any](resTree)
    }
  }
}
