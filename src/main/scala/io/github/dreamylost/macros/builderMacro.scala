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

  private final val BUFFER_CLASS_NAME_SUFFIX = "Builder"

  class BuilderProcessor(override val c: whitebox.Context) extends AbstractMacroProcessor(c) {

    import c.universe._

    private def getBuilderClassName(classTree: TypeName): TypeName = {
      TypeName(classTree.toTermName.decodedName.toString + BUFFER_CLASS_NAME_SUFFIX)
    }

    private def fieldDefinition(field: Tree): Tree = {
      field match {
        case q"$mods val $tname: $tpt = $expr" => q"""private var $tname: $tpt = $expr"""
        case q"$mods var $tname: $tpt = $expr" => q"""private var $tname: $tpt = $expr"""
      }
    }

    private def fieldSetMethod(typeName: TypeName, field: Tree, classTypeParams: List[Tree]): Tree = {
      val builderClassName = getBuilderClassName(typeName)
      val returnTypeParams = extractClassTypeParamsTypeName(classTypeParams)
      field match {
        case q"$mods var $tname: $tpt = $expr" =>
          q"""
              def $tname($tname: $tpt): $builderClassName[..$returnTypeParams] = {
                  this.$tname = $tname
                  this
              }
           """
        case q"$mods val $tname: $tpt = $expr" =>
          q"""
              def $tname($tname: $tpt): $builderClassName[..$returnTypeParams] = {
                  this.$tname = $tname
                  this
              }
           """
      }
    }

    private def getBuilderClassAndMethod(typeName: TypeName, fieldss: List[List[Tree]], classTypeParams: List[Tree], isCase: Boolean): Tree = {
      val fields = fieldss.flatten
      val builderClassName = getBuilderClassName(typeName)
      val builderFieldMethods = fields.map(f => fieldSetMethod(typeName, f, classTypeParams))
      val builderFieldDefinitions = fields.map(f => fieldDefinition(f))
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
      val (className, fieldss, classTypeParams) = classDecl match {
        // @see https://scala-lang.org/files/archive/spec/2.13/05-classes-and-objects.html
        case q"$mods class $tpname[..$tparams](...$paramss) extends ..$bases { ..$body }" =>
          c.info(c.enclosingPosition, s"modifiedDeclaration className: $tpname, paramss: $paramss", force = true)
          (tpname.asInstanceOf[TypeName], paramss.asInstanceOf[List[List[Tree]]], tparams.asInstanceOf[List[Tree]])
        case _ => c.abort(c.enclosingPosition, s"${ErrorMessage.ONLY_CLASS} classDef: $classDecl")
      }

      val builder = getBuilderClassAndMethod(className, fieldss, classTypeParams, isCaseClass(classDecl))
      val compDecl = modifiedCompanion(compDeclOpt, builder, className)
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
