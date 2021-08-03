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
object builderMacro {

  class BuilderProcessor(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

    import c.universe._

    private def getBuilderClassName(classTree: TypeName): TypeName = {
      TypeName(classTree.toTermName.decodedName.toString + "Builder")
    }

    private def getFieldDefinition(field: Tree): Tree = {
      val ValDef(mods, name, tpt, rhs) = field
      q"private var $name: $tpt = $rhs"
    }

    private def getFieldSetMethod(typeName: TypeName, field: Tree, classTypeParams: List[Tree]): Tree = {
      val builderClassName = getBuilderClassName(typeName)
      val returnTypeParams = extractClassTypeParamsTypeName(classTypeParams)
      lazy val valDefMapTo = (v: ValDef) => {
        q"""
          def ${v.name}(${v.name}: ${v.tpt}): $builderClassName[..$returnTypeParams] = {
              this.${v.name} = ${v.name}
              this
          }
         """
      }
      valDefMapTo(field.asInstanceOf[ValDef])
    }

    private def getBuilderClassAndMethod(typeName: TypeName, fieldss: List[List[Tree]], classTypeParams: List[Tree], isCase: Boolean): Tree = {
      val fields = fieldss.flatten
      val builderClassName = getBuilderClassName(typeName)
      val builderFieldMethods = fields.map(f => getFieldSetMethod(typeName, f, classTypeParams))
      val builderFieldDefinitions = fields.map(f => getFieldDefinition(f))
      val returnTypeParams = extractClassTypeParamsTypeName(classTypeParams)
      q"""
      def builder[..$classTypeParams](): $builderClassName[..$returnTypeParams] = new $builderClassName()

      class $builderClassName[..$classTypeParams] {

          ..$builderFieldDefinitions

          ..$builderFieldMethods

          def build(): $typeName[..$returnTypeParams] = ${getConstructorWithCurrying(typeName, fieldss, isCase)}
      }
       """
    }

    override def modifiedDeclaration(classDecl: ClassDef, compDeclOpt: Option[ModuleDef] = None): Any = {
      val classDefinition = mapClassDeclInfo(classDecl)
      val builder = getBuilderClassAndMethod(classDefinition.className, classDefinition.classParamss,
        classDefinition.classTypeParams, isCaseClass(classDecl))
      val compDecl = modifiedCompanion(compDeclOpt, builder, classDefinition.className)
      // Return both the class and companion object declarations
      c.Expr(
        q"""
        $classDecl
        $compDecl
      """)
    }

    override def impl(annottees: c.universe.Expr[Any]*): c.universe.Expr[Any] = {
      val resTree = handleWithImplType(annottees: _*)(modifiedDeclaration)
      printTree(force = true, resTree.tree)
      resTree
    }
  }

}
