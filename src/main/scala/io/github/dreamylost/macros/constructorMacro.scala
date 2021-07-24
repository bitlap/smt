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
object constructorMacro {

  class ConstructorProcessor(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

    import c.universe._

    override def impl(annottees: c.universe.Expr[Any]*): c.universe.Expr[Any] = {
      val args: (Boolean, Seq[String]) = extractArgumentsTuple2 {
        case q"new constructor(verbose=$verbose)" => (evalTree(verbose.asInstanceOf[Tree]), Nil)
        case q"new constructor(excludeFields=$excludeFields)" => (false, evalTree(excludeFields.asInstanceOf[Tree]))
        case q"new constructor(verbose=$verbose, excludeFields=$excludeFields)" => (evalTree(verbose.asInstanceOf[Tree]), evalTree(excludeFields.asInstanceOf[Tree]))
        case q"new constructor()" => (false, Nil)
        case _ => c.abort(c.enclosingPosition, ErrorMessage.UNEXPECTED_PATTERN)
      }

      val annotateeClass: ClassDef = checkAndGetClassDef(annottees: _*)
      val isCase: Boolean = isCaseClass(annotateeClass)
      if (isCase) {
        c.abort(c.enclosingPosition, s"${ErrorMessage.ONLY_CLASS} classDef: $annotateeClass")
      }

      def modifiedDeclaration(classDecl: ClassDef, compDeclOpt: Option[ModuleDef] = None): Any = {
        val (annotteeClassParams, annotteeClassDefinitions) = classDecl match {
          case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" =>
            c.info(c.enclosingPosition, s"modifiedDeclaration className: $tpname, paramss: $paramss", force = args._1)
            (paramss.asInstanceOf[List[List[Tree]]], stats.asInstanceOf[Seq[Tree]])
          case _ => c.abort(c.enclosingPosition, s"${ErrorMessage.ONLY_CLASS} classDef: $classDecl")
        }

        // Extract the internal fields of members belonging to the class， but not in primary constructor.
        val classFieldDefinitions = getClassMemberValDefs(annotteeClassDefinitions)
        val excludeFields = args._2

        /**
         * Extract the internal fields of members belonging to the class， but not in primary constructor and only `var`.
         */
        def getClassMemberVarDefOnlyAssignExpr: Seq[Tree] = {
          import c.universe._
          getClassMemberValDefs(annotteeClassDefinitions).filter(_ match {
            case q"$mods var $tname: $tpt = $expr" if !excludeFields.contains(tname.asInstanceOf[TermName].decodedName.toString) => true
            case _ => false
          }).map {
            case q"$mods var $pat = $expr" =>
              // TODO getClass RETURN a java type, maybe we can try use class reflect to get the fields type name.
              q"$pat: ${TypeName(toScalaType(evalTree(expr.asInstanceOf[Tree]).getClass.getTypeName))}"
            case q"$mods var $tname: $tpt = $expr" => q"$tname: $tpt"
          }
        }

        val classFieldDefinitionsOnlyAssignExpr = getClassMemberVarDefOnlyAssignExpr

        if (classFieldDefinitionsOnlyAssignExpr.isEmpty) {
          c.abort(c.enclosingPosition, s"Annotation is only supported on class when the internal field (declare as 'var') is nonEmpty. classDef: $classDecl")
        }

        val annotteeClassFieldNames = classFieldDefinitions.filter(_ match {
          case q"$mods var $tname: $tpt = $expr" if !excludeFields.contains(tname.asInstanceOf[TermName].decodedName.toString) => true
          case _ => false
        }).map {
          case q"$mods var $tname: $tpt = $expr" => tname.asInstanceOf[TermName]
        }

        c.info(c.enclosingPosition, s"modifiedDeclaration compDeclOpt: $compDeclOpt, annotteeClassParams: $annotteeClassParams", force = args._1)

        // Extract the field of the primary constructor.
        val allFieldsTermName = annotteeClassParams.map(f => f.map(ff => getFieldTermName(ff)))

        /**
         * We generate this method with currying, and we have to deal with the first layer of currying alone.
         */
        def getThisMethodWithCurrying: Tree = {
          // not currying
          // Extract the field of the primary constructor.
          val classParamsAssignExpr = getFieldAssignExprs(annotteeClassParams.flatten)
          val applyMethod = if (annotteeClassParams.isEmpty || annotteeClassParams.size == 1) {
            q"""
          def this(..${classParamsAssignExpr ++ classFieldDefinitionsOnlyAssignExpr}) = {
            this(..${allFieldsTermName.flatten})
            ..${annotteeClassFieldNames.map(f => q"this.$f = $f")}
          }
          """
          } else {
            // NOTE: currying constructor overload must be placed in the first bracket block.
            val allClassParamsAssignExpr = annotteeClassParams.map(cc => getFieldAssignExprs(cc))
            q"""
          def this(..${allClassParamsAssignExpr.head ++ classFieldDefinitionsOnlyAssignExpr})(...${allClassParamsAssignExpr.tail}) = {
            this(..${allFieldsTermName.head})(...${allFieldsTermName.tail})
            ..${annotteeClassFieldNames.map(f => q"this.$f = $f")}
          }
         """
          }
          applyMethod
        }

        val resTree = annotateeClass match {
          case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" =>
            q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..${stats.toList.:+(getThisMethodWithCurrying)} }"
        }
        c.Expr[Any](treeResultWithCompanionObject(resTree, annottees: _*))
      }

      val resTree = handleWithImplType(annottees: _*)(modifiedDeclaration)
      printTree(force = args._1, resTree.tree)

      resTree
    }
  }
}
