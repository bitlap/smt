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

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import scala.reflect.macros.whitebox

/**
 * Evict cache
 *
 * @author 梦境迷离
 * @since 2022/3/19
 * @version 1.0
 */
object CacheEvictMacro {

  class CacheEvictProcessor(val c: whitebox.Context) {

    import c.universe._

    private lazy val resultValName: c.universe.TermName = TermName("$result")
    private lazy val argsValName: c.universe.TermName = TermName("$args")

    private val parameters: Tuple2[Boolean, Seq[String]] = {
      c.prefix.tree match {
        case q"new cacheEvict(verbose=$verbose, values=$values)" =>
          Tuple2(
            c.eval(c.Expr[Boolean](c.untypecheck(verbose.asInstanceOf[Tree].duplicate))),
            c.eval(c.Expr[Seq[String]](c.untypecheck(values.asInstanceOf[Tree].duplicate)))
          )
        case q"new cacheEvict($verbose, values=$values)" =>
          Tuple2(
            c.eval(c.Expr[Boolean](c.untypecheck(verbose.asInstanceOf[Tree].duplicate))),
            c.eval(c.Expr[Seq[String]](c.untypecheck(values.asInstanceOf[Tree].duplicate)))
          )
        case q"new cacheEvict(values=$values)" =>
          Tuple2(
            false,
            c.eval(c.Expr[Seq[String]](c.untypecheck(values.asInstanceOf[Tree].duplicate)))
          )
        case q"new cacheEvict($values)" =>
          Tuple2(
            false,
            c.eval(c.Expr[Seq[String]](c.untypecheck(values.asInstanceOf[Tree].duplicate)))
          )
        case _ =>
          c.abort(c.enclosingPosition, "Unexpected annotation pattern!")
      }
    }

    private def getParamsName(vparamss: List[List[ValDef]]): List[List[TermName]] = {
      vparamss.map(_.map(_.name))
    }

    /**
     * Output ast result.
     *
     * @param force
     * @param resTree
     */
    private def printTree(force: Boolean, resTree: Tree): Unit = {
      c.info(
        c.enclosingPosition,
        s"\n###### Time: ${
          ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
        } " +
          s"Expanded macro start ######\n" + resTree.toString() + "\n###### Expanded macro end ######\n",
        force = force
      )
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
          if (parameters._2.isEmpty) {
            c.abort(c.enclosingPosition, s"The filed `values` cannot be empty!")
          }
          // TODO check values is valid
          val clazz = c.enclosingClass match {
            case ClassDef(_, name, _, Template(_, _, _)) => name.decodedName.toString
            case ModuleDef(_, name, Template(_, _, _))   => name.decodedName.toString
          }
          val newBody =
            q"""
             val $resultValName = ${defDef.rhs}
             val $argsValName = List(..${parameters._2}).map(p => $clazz + "-" + p)
             org.bitlap.tools.cacheable.Cache.evict($resultValName)(${argsValName}, ..${getParamsName(vparamss)})
           """
          DefDef(mods, name, tparams, vparamss, tpt, newBody)

      }
      printTree(force = parameters._1, resTree)
      c.Expr[Any](resTree)
    }
  }
}
