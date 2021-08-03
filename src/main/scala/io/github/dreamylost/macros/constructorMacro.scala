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

    private val extractArgumentsDetail: Tuple2[Boolean, Seq[String]] = {
      extractArgumentsTuple2 {
        case q"new constructor(verbose=$verbose)" => (evalTree(verbose.asInstanceOf[Tree]), Nil)
        case q"new constructor(excludeFields=$excludeFields)" => (false, evalTree(excludeFields.asInstanceOf[Tree]))
        case q"new constructor(verbose=$verbose, excludeFields=$excludeFields)" => (evalTree(verbose.asInstanceOf[Tree]), evalTree(excludeFields.asInstanceOf[Tree]))
        case q"new constructor()" => (false, Nil)
        case _ => c.abort(c.enclosingPosition, ErrorMessage.UNEXPECTED_PATTERN)
      }
    }

    private def getMutableValDefAndExcludeFields(annotteeClassDefinitions: Seq[Tree]): Seq[c.universe.ValDef] = {
      getClassMemberValDefs(annotteeClassDefinitions).filter(v => v.mods.hasFlag(Flag.MUTABLE) &&
        !extractArgumentsDetail._2.contains(v.name.decodedName.toString))
    }

    /**
     * Extract the internal fields of members belonging to the class， but not in primary constructor and only `var`.
     */
    private def getMemberVarDefTermNameWithType(annotteeClassDefinitions: Seq[Tree]): Seq[Tree] = {
      getMutableValDefAndExcludeFields(annotteeClassDefinitions).map { v =>
        if (v.tpt.isEmpty) { // val i = 1, tpt is `<type ?>`
          // TODO getClass RETURN a java type, maybe we can try use class reflect to get the fields type name.
          q"${v.name}: ${TypeName(toScalaType(evalTree(v.rhs).getClass.getTypeName))}"
        } else {
          q"${v.name}: ${v.tpt}"
        }
      }
    }

    /**
     * We generate this method with currying, and we have to deal with the first layer of currying alone.
     */
    private def getThisMethodWithCurrying(annotteeClassParams: List[List[Tree]], annotteeClassDefinitions: Seq[Tree]): Tree = {
      val classInternalFieldsWithType = getMemberVarDefTermNameWithType(annotteeClassDefinitions)

      if (classInternalFieldsWithType.isEmpty) {
        c.abort(c.enclosingPosition, s"${ErrorMessage.ONLY_CLASS} and the internal fields (declare as 'var') should not be Empty.")
      }
      // Extract the internal fields of members belonging to the class， but not in primary constructor.
      val annotteeClassFieldNames = getMutableValDefAndExcludeFields(annotteeClassDefinitions).map(_.name)
      val allFieldsTermName = getClassConstructorValDefsNotFlatten(annotteeClassParams).map(_.map(_.name.toTermName))
      // Extract the field of the primary constructor.
      val classParamsNameWithType = getConstructorParamsNameWithType(annotteeClassParams.flatten)
      val applyMethod = if (annotteeClassParams.isEmpty || annotteeClassParams.size == 1) {
        q"""
          def this(..${classParamsNameWithType ++ classInternalFieldsWithType}) = {
            this(..${allFieldsTermName.flatten})
            ..${annotteeClassFieldNames.map(f => q"this.$f = $f")}
          }
          """
      } else {
        // NOTE: currying constructor overload must be placed in the first bracket block.
        val allClassCtorParamsNameWithType = annotteeClassParams.map(cc => getConstructorParamsNameWithType(cc))
        q"""
          def this(..${allClassCtorParamsNameWithType.head ++ classInternalFieldsWithType})(...${allClassCtorParamsNameWithType.tail}) = {
            this(..${allFieldsTermName.head})(...${allFieldsTermName.tail})
            ..${annotteeClassFieldNames.map(f => q"this.$f = $f")}
          }
         """
      }
      applyMethod
    }

    override def modifiedDeclaration(classDecl: ClassDef, compDeclOpt: Option[ModuleDef] = None): Any = {
      val resTree = appendedBody(
        classDecl,
        classInfo => Seq(getThisMethodWithCurrying(classInfo.classParamss, classInfo.body)))
      c.Expr(resTree)
    }

    override def impl(annottees: c.universe.Expr[Any]*): c.universe.Expr[Any] = {
      val annotateeClass: ClassDef = checkAndGetClassDef(annottees: _*)
      if (isCaseClass(annotateeClass)) c.abort(c.enclosingPosition, s"${ErrorMessage.ONLY_CLASS} classDef: $annotateeClass")
      val tmpTree = handleWithImplType(annottees: _*)(modifiedDeclaration)
      val res = treeReturnWithDefaultCompanionObject(tmpTree.tree, annottees: _*)
      printTree(force = extractArgumentsDetail._1, res)
      c.Expr(res)
    }
  }

}
