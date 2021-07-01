package io.github.dreamylost

import scala.annotation.{ StaticAnnotation, compileTimeOnly }
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

/**
 * annotation to generate builder pattern for classes.
 *
 * @author 梦境迷离
 * @since 2021/6/19
 * @version 1.0
 */
@compileTimeOnly("enable macro to expand macro annotations")
final class builder extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro builderMacro.impl

}

object builderMacro extends MacroCommon {

  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    // @see https://scala-lang.org/files/archive/spec/2.13/05-classes-and-objects.html
    def fieldSetMethod(c: whitebox.Context)(field: c.universe.Tree): c.universe.Tree = {
      import c.universe._
      field match {
        case tree @ q"$mods var $tname: $tpt = $expr" =>
          q"""
              def $tname($tname: $tpt): Builder = {
                  this.$tname = $tname
                  this
              }
           """
        case tree @ q"$mods val $tname: $tpt = $expr" =>
          q"""
              def $tname($tname: $tpt): Builder = {
                  this.$tname = $tname
                  this
              }
           """
      }
    }

    def fieldDefinitionMethod(c: whitebox.Context)(field: c.universe.Tree): c.universe.Tree = {
      import c.universe._
      field match {
        case tree @ q"$mods val $tname: $tpt = $expr" => q"""private var $tname: $tpt = $expr"""
        case tree @ q"$mods var $tname: $tpt = $expr" => q"""private var $tname: $tpt = $expr"""
      }
    }

    def fieldTermNameMethod(c: whitebox.Context)(field: c.universe.Tree): c.universe.Tree = {
      import c.universe._
      field match {
        case tree @ q"$mods val $tname: $tpt = $expr" => q"""$tname"""
        case tree @ q"$mods var $tname: $tpt = $expr" => q"""$tname"""
      }
    }

    def builderTemplate(typeName: TypeName, fields: List[Tree], isCase: Boolean): c.universe.Tree = {
      val termName = typeName.toTermName.toTermName
      val builderFieldMethods = fields.map(f => fieldSetMethod(c)(f))
      val builderFieldDefinitions = fields.map(f => fieldDefinitionMethod(c)(f))
      val allFieldsTermName = fields.map(f => fieldTermNameMethod(c)(f))
      q"""
      def builder(): Builder = new Builder()

      class Builder {

          ..$builderFieldDefinitions

          ..$builderFieldMethods

          def build(): $typeName = ${if (isCase) q"$termName(..$allFieldsTermName)" else q"new $typeName(..$allFieldsTermName)"}
      }
       """
    }

    // The dependent type need aux-pattern in scala2. Now let's get around this.
    def modifiedDeclaration(classDecl: ClassDef, compDeclOpt: Option[ModuleDef] = None): Any = {
      val (className, fields) = classDecl match {
        case q"$mods class $tpname[..$tparams](...$paramss) extends ..$bases { ..$body }" =>
          c.info(c.enclosingPosition, s"modifiedDeclaration className: $tpname, paramss: $paramss", force = true)
          (tpname, paramss)
        case _ => c.abort(c.enclosingPosition, s"Annotation is only supported on class. classDef: $classDecl")
      }
      c.info(c.enclosingPosition, s"modifiedDeclaration compDeclOpt: $compDeclOpt, fields: $fields", force = true)

      val cName = className match {
        case t: TypeName => t
      }
      val isCase = isCaseClass(c)(classDecl)
      val builder = builderTemplate(cName, fields.asInstanceOf[List[List[Tree]]].flatten, isCase)
      val compDecl = modifiedCompanion(c)(compDeclOpt, builder, cName)
      c.info(c.enclosingPosition, s"builder: $builder, compDecl: $compDecl", force = true)
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
