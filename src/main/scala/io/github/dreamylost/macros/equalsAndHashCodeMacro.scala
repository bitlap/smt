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
 * @since 2021/7/18
 * @version 1.0
 */
object equalsAndHashCodeMacro {

  class EqualsAndHashCodeProcessor(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

    import c.universe._

    private val extractArgumentsDetail: (Boolean, Nil.type) = extractArgumentsTuple2 {
      case q"new equalsAndHashCode(verbose=$verbose)" => (evalTree(verbose.asInstanceOf[Tree]), Nil)
      case q"new equalsAndHashCode(excludeFields=$excludeFields)" => (false, evalTree(excludeFields.asInstanceOf[Tree]))
      case q"new equalsAndHashCode(verbose=$verbose, excludeFields=$excludeFields)" => (evalTree(verbose.asInstanceOf[Tree]), evalTree(excludeFields.asInstanceOf[Tree]))
      case q"new equalsAndHashCode()" => (false, Nil)
      case _ => c.abort(c.enclosingPosition, ErrorMessage.UNEXPECTED_PATTERN)
    }

    override def impl(annottees: c.universe.Expr[Any]*): c.universe.Expr[Any] = {
      val annotateeClass: ClassDef = checkGetClassDef(annottees)
      if (isCaseClass(annotateeClass)) {
        c.abort(c.enclosingPosition, ErrorMessage.ONLY_CLASS)
      }
      val resTree = collectCustomExpr(annottees)(createCustomExpr)
      printTree(force = extractArgumentsDetail._1, resTree.tree)
      resTree
    }

    /**
     * Extract the internal fields of members belonging to the class.
     */
    private def getInternalFieldsTermNameExcludeLocal(annotteeClassDefinitions: Seq[Tree]): Seq[TermName] = {
      if (annotteeClassDefinitions.exists(f => isNotLocalClassMember(f))) {
        c.info(c.enclosingPosition, s"There is a non private class definition inside the class", extractArgumentsDetail._1)
      }
      getClassMemberValDefs(annotteeClassDefinitions).filter(p => isNotLocalClassMember(p) &&
        !extractArgumentsDetail._2.contains(p.name.decodedName.toString)).map(_.name.toTermName)
    }

    // equals method
    private def getEqualsMethod(className: TypeName, termNames: Seq[TermName], superClasses: Seq[Tree], annotteeClassDefinitions: Seq[Tree]): List[Tree] = {
      val existsCanEqual = getClassMemberDefDefs(annotteeClassDefinitions).exists {
        case defDef: DefDef if defDef.name.decodedName.toString == "canEqual" && defDef.vparamss.nonEmpty =>
          val safeValDefs = valDefAccessors(defDef.vparamss.flatten)
          safeValDefs.exists(_.paramType.toString == "Any") && safeValDefs.exists(_.name.decodedName.toString == "that")
        case _ => false
      }
      val equalsExprs = termNames.map(termName => q"this.$termName.equals(t.$termName)")
      // Make a rough judgment on whether override is needed.
      val modifiers = if (existsSuperClassExcludeSdkClass(superClasses)) Modifiers(Flag.OVERRIDE, typeNames.EMPTY, List()) else Modifiers(NoFlags, typeNames.EMPTY, List())
      val canEqual = if (existsCanEqual) q"" else q"$modifiers def canEqual(that: Any) = that.isInstanceOf[$className]"
      val equalsMethod =
        q"""
          override def equals(that: Any): Boolean =
            that match {
              case t: $className => t.canEqual(this) && Seq(..$equalsExprs).forall(f => f) && ${if (existsSuperClassExcludeSdkClass(superClasses)) q"super.equals(that)" else q"true"}
              case _ => false
          }
         """
      List(canEqual, equalsMethod)
    }

    private def getHashcodeMethod(termNames: Seq[TermName], superClasses: Seq[Tree]): Tree = {
      // we append super.hashCode by `+`
      // the algorithm see https://alvinalexander.com/scala/how-to-define-equals-hashcode-methods-in-scala-object-equality/
      val superTree = q"super.hashCode"
      q"""
         override def hashCode(): Int = {
            val state = Seq(..$termNames)
            state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b) + ${if (existsSuperClassExcludeSdkClass(superClasses)) superTree else q"0"}
          }
       """
    }

    override def createCustomExpr(classDecl: ClassDef, compDeclOpt: Option[ModuleDef]): Any = {
      lazy val map = (classDefinition: ClassDefinition) => {
        getClassConstructorValDefsFlatten(classDefinition.classParamss).
          filter(cf => isNotLocalClassMember(cf)).
          map(_.name.toTermName) ++
          getInternalFieldsTermNameExcludeLocal(classDefinition.body)
      }
      val classDefinition = mapToClassDeclInfo(classDecl)
      val res = appendClassBody(classDecl, classInfo =>
        getEqualsMethod(classDefinition.className, map(classInfo), classDefinition.superClasses, classDefinition.body) ++
          List(getHashcodeMethod(map(classInfo), classDefinition.superClasses))
      )

      c.Expr(
        q"""
          ${compDeclOpt.fold(EmptyTree)(x => x)}
          $res
         """)
    }
  }

}
