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
      val annotateeClass: ClassDef = checkAndGetClassDef(annottees: _*)
      if (isCaseClass(annotateeClass)) c.abort(c.enclosingPosition, s"${ErrorMessage.ONLY_CLASS} classDef: $annotateeClass")

      val tmpTree = handleWithImplType(annottees: _*)(modifiedDeclaration)
      // return with object if it exists
      val resTree = annotateeClass match {
        case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" =>
          val originalStatus = q"{ ..$stats }"
          val append =
            q"""
              ..$originalStatus
              ..$tmpTree
             """
          q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..${append} }"
      }
      val res = c.Expr[Any](treeResultWithCompanionObject(resTree, annottees: _*))
      printTree(force = extractArgumentsDetail._1, res.tree)
      res
    }

    /**
     * Extract the internal fields of members belonging to the class.
     */
    private def getInternalFieldTermNameExcludeLocal(annotteeClassDefinitions: Seq[Tree]): Seq[TermName] = {
      getClassMemberValDefs(annotteeClassDefinitions).filter(p => p match {
        case q"$mods var $tname: $tpt = $expr" =>
          !extractArgumentsDetail._2.contains(tname.asInstanceOf[TermName].decodedName.toString) && classParamsIsNotLocal(p)
        case q"$mods val $tname: $tpt = $expr" =>
          !extractArgumentsDetail._2.contains(tname.asInstanceOf[TermName].decodedName.toString) && classParamsIsNotLocal(p)
        case q"$mods val $pat = $expr" =>
          !extractArgumentsDetail._2.contains(pat.asInstanceOf[TermName].decodedName.toString) && classParamsIsNotLocal(p)
        case q"$mods var $pat = $expr" =>
          !extractArgumentsDetail._2.contains(pat.asInstanceOf[TermName].decodedName.toString) && classParamsIsNotLocal(p)
        case _ => false
      }).map(f => getFieldTermName(f))
    }

    // equals method
    private def getEqualsMethod(className: TypeName, termNames: Seq[TermName], superClasses: Seq[Tree], annotteeClassDefinitions: Seq[Tree]): Tree = {
      val existsCanEqual = getClassMemberDefDefs(annotteeClassDefinitions) exists {
        case q"$mods def $tname[..$tparams](...$paramss): $tpt = $expr" if tname.asInstanceOf[TermName].decodedName.toString == "canEqual" && paramss.nonEmpty =>
          val params = paramss.asInstanceOf[List[List[Tree]]].flatten.map(pp => getMethodParamName(pp))
          params.exists(p => p.decodedName.toString == "Any")
        case _ => false
      }
      lazy val getEqualsExpr = (termName: TermName) => {
        q"this.$termName.equals(t.$termName)"
      }
      val equalsExprs = termNames.map(getEqualsExpr)
      // Make a rough judgment on whether override is needed.
      val modifiers = if (existsSuperClassExcludeSdkClass(superClasses)) Modifiers(Flag.OVERRIDE, typeNames.EMPTY, List()) else Modifiers(NoFlags, typeNames.EMPTY, List())
      val canEqual = if (existsCanEqual) q"" else q"$modifiers def canEqual(that: Any) = that.isInstanceOf[$className]"
      q"""
        $canEqual

        override def equals(that: Any): Boolean =
          that match {
            case t: $className => t.canEqual(this) && Seq(..$equalsExprs).forall(f => f) && ${if (existsSuperClassExcludeSdkClass(superClasses)) q"super.equals(that)" else q"true"}
            case _ => false
        }
       """
    }

    private def getHashcodeMethod(termNames: Seq[TermName], superClasses: Seq[Tree]): Tree = {
      // we append super.hashCode by `+`
      // the algorithm see https://alvinalexander.com/scala/how-to-define-equals-hashcode-methods-in-scala-object-equality/
      if (!existsSuperClassExcludeSdkClass(superClasses)) {
        q"""
         override def hashCode(): Int = {
            val state = Seq(..$termNames)
            state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
          }
          """
      } else {
        q"""
         override def hashCode(): Int = {
            val state = Seq(..$termNames)
            state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b) + super.hashCode
          }
          """
      }
    }

    override def modifiedDeclaration(classDecl: ClassDef, compDeclOpt: Option[ModuleDef]): Any = {
      val (className, annotteeClassParams, annotteeClassDefinitions, superClasses) = classDecl match {
        case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" =>
          (tpname.asInstanceOf[TypeName], paramss.asInstanceOf[List[List[Tree]]], stats.asInstanceOf[Seq[Tree]], parents.asInstanceOf[Seq[Tree]])
        case _ => c.abort(c.enclosingPosition, s"${ErrorMessage.ONLY_CLASS} classDef: $classDecl")
      }
      val ctorFieldNames = annotteeClassParams.flatten.filter(cf => classParamsIsNotLocal(cf))
      val allFieldsTermName = ctorFieldNames.map(f => getFieldTermName(f))
      val allTernNames = allFieldsTermName ++ getInternalFieldTermNameExcludeLocal(annotteeClassDefinitions)
      val hash = getHashcodeMethod(allTernNames, superClasses)
      val equals = getEqualsMethod(className, allTernNames, superClasses, annotteeClassDefinitions)
      c.Expr(
        q"""
          ..$equals
          $hash
         """)
    }
  }

}
