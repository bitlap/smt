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

package org.bitlap.csv.core.macros

import org.bitlap.csv.core.{ Scalable, ScalableBuilder }

import scala.collection.mutable
import scala.reflect.macros.whitebox

class DeriveScalableBuilder(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

  import c.universe._

  private val packageName = q"_root_.org.bitlap.csv.core"

  private val annoBuilderPrefix = "AnonScalableBuilder$"

  def setFieldImpl[T: c.WeakTypeTag, SF: c.WeakTypeTag, TF: c.WeakTypeTag](scalaField: c.Expr[T ⇒ SF], value: c.Expr[String ⇒ SF]): c.Expr[ScalableBuilder[T]] = {
    val Function(_, Select(_, termName)) = scalaField.tree
    val builderId = getBuilderId()
    MacroCache.builderFunctionTrees.getOrElseUpdate(builderId, mutable.Map.empty).update(termName.toString, value)
    val tree = q"new ${c.prefix.actualType}"
    printTree[ScalableBuilder[T]](force = true, tree)
  }

  def applyImpl[T: c.WeakTypeTag]: c.Expr[ScalableBuilder[T]] = {
    deriveBuilderApplyImpl[T]
  }

  def buildImpl[T: c.WeakTypeTag](line: c.Expr[String], columnSeparator: c.Expr[Char]): c.Expr[Scalable[T]] = {
    deriveScalableImpl[T](line, columnSeparator)
  }

  private def deriveBuilderApplyImpl[T: WeakTypeTag]: c.Expr[ScalableBuilder[T]] = {
    val className = TypeName(annoBuilderPrefix + MacroCache.getBuilderId)
    val caseClazzName = TypeName(c.weakTypeOf[T].typeSymbol.name.decodedName.toString)
    val tree =
      q"""
       class $className extends $packageName.ScalableBuilder[$caseClazzName]
       new $className
     """
    printTree[ScalableBuilder[T]](force = true, tree)

  }

  private val builderFunctionPrefix = "builderFunction$"

  private def deriveScalableImpl[T: c.WeakTypeTag](line: c.Expr[String], columnSeparator: c.Expr[Char]): c.Expr[Scalable[T]] = {
    val clazzName = TypeName(c.weakTypeOf[T].typeSymbol.name.decodedName.toString)
    val customTrees = MacroCache.builderFunctionTrees.getOrElse(getBuilderId(), mutable.Map.empty)
    val (_, preTrees) = customTrees.collect {
      case (key, expr: Expr[Tree]) ⇒
        expr.tree match {
          case buildFunction: Function ⇒
            val functionName = TermName(builderFunctionPrefix + key)
            key -> q"val $functionName = $buildFunction"
        }
    }.unzip
    val tree =
      q"""
       new $packageName.Scalable[$clazzName] {
          ..$preTrees
          ..${scalableBody[T](clazzName, line, columnSeparator)}
       }
    """
    printTree[Scalable[T]](force = true, tree)
  }

  private def scalableBody[T: c.WeakTypeTag](clazzName: TypeName, line: c.Expr[String], columnSeparator: c.Expr[Char]): Tree = {
    val customTrees = MacroCache.builderFunctionTrees.getOrElse(getBuilderId(), mutable.Map.empty)
    val params = getCaseClassParams[T]()
    val paramsName = params.map(_.name.decodedName.toString)
    var i = 0
    val fields = checkCaseClassReturnConstructorParams[T](line, columnSeparator).map { idxType =>
      val fieldTypeName = TypeName(idxType._2.typeSymbol.name.decodedName.toString)
      val innerTree = if (idxType._2 <:< typeOf[Option[_]]) {
        val genericType = c.typecheck(q"${idxType._2}", c.TYPEmode).tpe.typeArgs.head
        if (customTrees.contains(paramsName(i))) {
          q"""
              val functionRet = ${TermName(builderFunctionPrefix + paramsName(i))}.apply(${idxType._1})
              $packageName.Scalable[${genericType.typeSymbol.name.toTypeName}]._toScala(if (functionRet.isEmpty) "" else functionRet.get)
              """
        } else {
          q"$packageName.Scalable[${genericType.typeSymbol.name.toTypeName}]._toScala(${idxType._1})"
        }
      } else {
        if (customTrees.contains(fieldTypeName.toString)) {
          val customFunction = customTrees(fieldTypeName.toString).asInstanceOf[Tree]
          q"$packageName.Scalable[$fieldTypeName]._toScala($customFunction(${idxType._1})).getOrElse(null)"
        } else {
          idxType._2 match {
            case t if t =:= typeOf[Int] =>
              q"$packageName.Scalable[$fieldTypeName]._toScala(${idxType._1}).getOrElse(0)"
            case t if t =:= typeOf[String] =>
              q"""$packageName.Scalable[$fieldTypeName]._toScala(${idxType._1}).getOrElse("")"""
            case t if t =:= typeOf[Float] =>
              q"$packageName.Scalable[$fieldTypeName]._toScala(${idxType._1}).getOrElse[Float](0.asInstanceOf[Float])"
            case t if t =:= typeOf[Double] =>
              q"$packageName.Scalable[$fieldTypeName]._toScala(${idxType._1}).getOrElse[Double](0D)"
            case t if t =:= typeOf[Char] =>
              q"$packageName.Scalable[$fieldTypeName]._toScala(${idxType._1}).getOrElse('?')"
            case t if t =:= typeOf[Byte] =>
              q"$packageName.Scalable[$fieldTypeName]._toScala(${idxType._1}).getOrElse(0)"
            case t if t =:= typeOf[Short] =>
              q"$packageName.Scalable[$fieldTypeName]._toScala(${idxType._1}).getOrElse(0)"
            case t if t =:= typeOf[Boolean] =>
              q"$packageName.Scalable[$fieldTypeName]._toScala(${idxType._1}).getOrElse(false)"
            case t if t =:= typeOf[Long] =>
              q"$packageName.Scalable[$fieldTypeName]._toScala(${idxType._1}).getOrElse(0L)"
          }
        }
      }
      i += 1
      innerTree
    }

    q"""
       override def toScala: Option[$clazzName] = Option(${TermName(clazzName.decodedName.toString)}(..$fields)) 
     """
  }

  private def getBuilderId(): Int = {
    c.prefix.actualType.toString.replace(annoBuilderPrefix, "").toInt
  }
}
