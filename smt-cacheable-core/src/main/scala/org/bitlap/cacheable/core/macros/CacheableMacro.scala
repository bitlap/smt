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

package org.bitlap.cacheable.core.macros

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

    protected val local: Boolean = {
      c.prefix.tree match {
        case q"new cacheable(local=$local)" =>
          c.eval(c.Expr[Boolean](c.untypecheck(local.asInstanceOf[Tree].duplicate)))
        case q"new cacheable($local)" => c.eval(c.Expr[Boolean](c.untypecheck(local.asInstanceOf[Tree].duplicate)))
        case q"new cacheable()"       => true
      }
    }

    private def getParamsName(vparamss: List[List[ValDef]]): List[List[TermName]] =
      vparamss.map(_.map(_.name))

    def impl(annottees: c.universe.Expr[Any]*): c.universe.Expr[Any] = {
      val resTree = annottees.map(_.tree) match {
        case (defDef @ DefDef(mods, name, tparams, vparamss, tpt, _)) :: Nil =>
          if (tpt.isEmpty) {
            c.abort(c.enclosingPosition, "The return type of the method is not specified!")
          }
          val tp = c.typecheck(tq"$tpt", c.TYPEmode).tpe
          if (!(tp <:< typeOf[zio.ZIO[_, _, _]]) && !(tp <:< typeOf[zio.stream.ZStream[_, _, _]])) {
            c.abort(
              c.enclosingPosition,
              s"The return type of the method not support type: `${tp.typeSymbol.name.toString}`!"
            )
          }
          val importExpr =
            if (local) q"import _root_.org.bitlap.cacheable.caffeine.Implicits._"
            else q"import _root_.org.bitlap.cacheable.redis.Implicits._"
          val newBody =
            q"""
             val $resultValName = ${defDef.rhs}
             val $keyValName = List($getEnclosingClassName, ${name.decodedName.toString})
             $importExpr
             org.bitlap.cacheable.core.Cache($resultValName)($keyValName, ..${getParamsName(vparamss)})
           """
          DefDef(mods, name, tparams, vparamss, tpt, newBody)

      }
      printTree(force = false, resTree)
      c.Expr[Any](resTree)
    }
  }
}
