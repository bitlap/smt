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

package io.github.dreamylost.di

import scala.reflect.macros.whitebox
import annotation.tailrec

/**
 * DI Macro
 *
 * @author 梦境迷离
 * @since 2021/7/22
 * @version 1.0
 */
object DiMacro {

  def AutoWire[T]: T = macro AutoWireMacro[T]

  def AutoWireMacro[T: c.WeakTypeTag](c: whitebox.Context): c.Expr[T] = {
    import c.universe._

    // TODO: this should check if the found value uses AutoWire[]; now any value of the desired type will be returned
    def findWiredOfType(t: Type): Option[Name] = {
      @tailrec
      def doFind(trees: List[Tree]): Option[Name] = trees match {
        case Nil =>
          c.error(c.enclosingPosition, s"Cannot find a wired value of type $t")
          None
        case tree :: tail => tree match {
          // TODO: subtyping
          case ValDef(_, name, tpt, _) if (tpt.tpe weak_<:< t) || (tpt.tpe <:< t) => Some(name.encodedName)
          case DefDef(defDef) if (defDef._5.tpe weak_<:< t) || (defDef._5.tpe <:< t) => Some(defDef._2.decodedName)
          case _ => doFind(tail)
        }
      }

      val ClassDef(_, _, _, Template(_, _, body)) = c.enclosingClass
      doFind(body)
    }

    val tType = implicitly[c.WeakTypeTag[T]]
    val tConstructorOpt = tType.tpe.members.find(_.name.toTermName.toTermName.toString == "<init>")
    val result = tConstructorOpt match {
      case None =>
        c.error(c.enclosingPosition, "Cannot find constructor for " + tType)
        reify {
          null.asInstanceOf[T]
        }
      case Some(tConstructor) =>
        val params = tConstructor.asMethod.paramLists.flatten

        val newT = Select(New(Ident(tType.tpe.typeSymbol)), termNames.CONSTRUCTOR)

        val constructorParams = for (param <- params) yield {
          val wireTo = findWiredOfType(param.typeSignature)
            // If we cannot find a value of the given type, trying a by-name match, using the same name as the constructor's parameter.
            .getOrElse(param.name)
          Ident(wireTo)
        }

        val newTWithParams = Apply(newT, constructorParams)
        c.info(c.enclosingPosition, s"Generated code: ${c.universe.show(newTWithParams)}", force = false)
        c.Expr(newTWithParams)
    }
    result
  }
}
