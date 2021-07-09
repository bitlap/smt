package io.github.dreamylost.macros

import scala.reflect.macros.whitebox

/**
 *
 * @author 梦境迷离
 * @since 2021/7/7
 * @version 1.0
 */
object builderMacro extends MacroCommon {
  private final val BUFFER_CLASS_NAME_SUFFIX = "Builder"

  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def getBuilderClassName(classTree: TypeName): TypeName = {
      TypeName(classTree.toTermName.decodedName.toString + BUFFER_CLASS_NAME_SUFFIX)
    }

    def fieldSetMethod(typeName: TypeName, field: Tree): c.Tree = {
      val builderClassName = getBuilderClassName(typeName)
      field match {
        case q"$mods var $tname: $tpt = $expr" =>
          q"""
              def $tname($tname: $tpt): ${builderClassName} = {
                  this.$tname = $tname
                  this
              }
           """
        case q"$mods val $tname: $tpt = $expr" =>
          q"""
              def $tname($tname: $tpt): ${builderClassName} = {
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

    def builderTemplate(typeName: TypeName, fieldss: List[List[Tree]], isCase: Boolean): Tree = {
      val fields = fieldss.flatten
      val builderClassName = getBuilderClassName(typeName)
      val builderFieldMethods = fields.map(f => fieldSetMethod(typeName, f))
      val builderFieldDefinitions = fields.map(f => fieldDefinition(f))
      q"""
      def builder(): $builderClassName = new $builderClassName()

      class $builderClassName {

          ..$builderFieldDefinitions

          ..$builderFieldMethods

          def build(): $typeName = ${getConstructorWithCurrying(c)(typeName, fieldss, isCase)}
      }
       """
    }

    // Why use Any? The dependent type need aux-pattern in scala2. Now let's get around this.
    def modifiedDeclaration(classDecl: ClassDef, compDeclOpt: Option[ModuleDef] = None): Any = {
      val (className, fieldss) = classDecl match {
        // @see https://scala-lang.org/files/archive/spec/2.13/05-classes-and-objects.html
        case q"$mods class $tpname[..$tparams](...$paramss) extends ..$bases { ..$body }" =>
          c.info(c.enclosingPosition, s"modifiedDeclaration className: $tpname, paramss: $paramss", force = true)
          (tpname, paramss)
        case _ => c.abort(c.enclosingPosition, s"${ErrorMessage.ONLY_CLASS} classDef: $classDecl")
      }
      c.info(c.enclosingPosition, s"modifiedDeclaration compDeclOpt: $compDeclOpt, fieldss: $fieldss", force = true)

      val cName = className match {
        case t: TypeName => t
      }
      val isCase = isCaseClass(c)(classDecl)
      val builder = builderTemplate(cName, fieldss, isCase)
      val compDecl = modifiedCompanion(c)(compDeclOpt, builder, cName)
      c.info(c.enclosingPosition, s"builderTree: $builder, compDecl: $compDecl", force = true)
      // Return both the class and companion object declarations
      c.Expr(
        q"""
        $classDecl
        $compDecl
      """)
    }

    c.info(c.enclosingPosition, s"builder annottees: $annottees", force = true)

    val resTree = handleWithImplType(c)(annottees: _*)(modifiedDeclaration)
    printTree(c)(force = true, resTree.tree)

    resTree
  }
}
