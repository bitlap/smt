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

    override def impl(annottees: c.universe.Expr[Any]*): c.universe.Expr[Any] = {
      def getBuilderClassName(classTree: TypeName): TypeName = {
        TypeName(classTree.toTermName.decodedName.toString + BUFFER_CLASS_NAME_SUFFIX)
      }

      def fieldSetMethod(typeName: TypeName, field: Tree, classTypeParams: List[Tree]): Tree = {
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

      def fieldDefinition(field: Tree): Tree = {
        field match {
          case q"$mods val $tname: $tpt = $expr" => q"""private var $tname: $tpt = $expr"""
          case q"$mods var $tname: $tpt = $expr" => q"""private var $tname: $tpt = $expr"""
        }
      }

      def builderTemplate(typeName: TypeName, fieldss: List[List[Tree]], classTypeParams: List[Tree], isCase: Boolean): Tree = {
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

      // Why use Any? The dependent type need aux-pattern in scala2. Now let's get around this.
      def modifiedDeclaration(classDecl: ClassDef, compDeclOpt: Option[ModuleDef] = None): Any = {
        val (className, fieldss, classTypeParams) = classDecl match {
          // @see https://scala-lang.org/files/archive/spec/2.13/05-classes-and-objects.html
          case q"$mods class $tpname[..$tparams](...$paramss) extends ..$bases { ..$body }" =>
            c.info(c.enclosingPosition, s"modifiedDeclaration className: $tpname, paramss: $paramss", force = true)
            (tpname, paramss.asInstanceOf[List[List[Tree]]], tparams.asInstanceOf[List[Tree]])
          case _ => c.abort(c.enclosingPosition, s"${ErrorMessage.ONLY_CLASS} classDef: $classDecl")
        }

        val cName = className.asInstanceOf[TypeName]
        val isCase = isCaseClass(classDecl)
        val builder = builderTemplate(cName, fieldss, classTypeParams, isCase)
        val compDecl = modifiedCompanion(compDeclOpt, builder, cName)
        c.info(c.enclosingPosition, s"builderTree: $builder, compDecl: $compDecl", force = true)
        // Return both the class and companion object declarations
        c.Expr(
          q"""
        $classDecl
        $compDecl
      """)
      }

      val resTree = handleWithImplType(annottees: _*)(modifiedDeclaration)
      printTree(force = true, resTree.tree)

      resTree
    }
  }

}
