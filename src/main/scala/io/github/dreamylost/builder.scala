package io.github.dreamylost

import scala.annotation.{ StaticAnnotation, compileTimeOnly }
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

/**
 *
 * @author 梦境迷离
 * @since 2021/6/19
 * @version 1.0
 */
@compileTimeOnly("enable macro to expand macro annotations")
final class builder extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro builderMacro.impl

}

object builderMacro {

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

    def modifiedCompanion(compDeclOpt: Option[ModuleDef], builder: Tree, className: TypeName): c.universe.Tree = {
      compDeclOpt map { compDecl =>
        // Add the builder to the existing companion object
        val q"object $obj extends ..$bases { ..$body }" = compDecl
        val o =
          q"""
          object $obj extends ..$bases {
            ..$body
            ..$builder
          }
        """
        c.info(c.enclosingPosition, s"modifiedCompanion className: $className, exists obj: $o", force = true)
        o
      } getOrElse {
        // Create a companion object with the builder
        val o = q"object ${className.toTermName} { ..$builder }"
        c.info(c.enclosingPosition, s"modifiedCompanion className: $className, new obj: $o", force = true)
        o
      }
    }

    def modifiedDeclaration(classDecl: ClassDef, compDeclOpt: Option[ModuleDef] = None): c.Expr[Nothing] = {
      val (mods, className, fields) = classDecl match {
        case q"$mods class $className(..$fields) extends ..$bases { ..$body }" =>
          c.info(c.enclosingPosition, s"modifiedDeclaration className: $className, fields: $fields", force = true)
          (mods, className, fields)
        case _ => c.abort(c.enclosingPosition, s"Annotation is only supported on class. classDef: $classDecl")
      }
      c.info(c.enclosingPosition, s"modifiedDeclaration compDeclOpt: $compDeclOpt, fields: $fields", force = true)
      className match {
        case tp: TypeName =>
          val builder = builderTemplate(tp, fields.asInstanceOf[List[Tree]], mods.asInstanceOf[Modifiers].hasFlag(Flag.CASE))
          val compDecl = modifiedCompanion(compDeclOpt, builder, tp)
          c.info(c.enclosingPosition, s"builder: $builder, compDecl: $compDecl", force = true)
          // Return both the class and companion object declarations
          c.Expr(
            q"""
        $classDecl
        $compDecl
      """)
      }

    }

    c.info(c.enclosingPosition, s"builder annottees: $annottees", true)

    val resTree = annottees.map(_.tree) match {
      case (classDecl: ClassDef) :: Nil => modifiedDeclaration(classDecl)
      case (classDecl: ClassDef) :: (compDecl: ModuleDef) :: Nil => modifiedDeclaration(classDecl, Some(compDecl))
      case _ => c.abort(c.enclosingPosition, "Invalid annottee")
    }

    // Print the ast
    c.info(
      c.enclosingPosition,
      "\n###### Expanded macro ######\n" + resTree.toString() + "\n###### Expanded macro ######\n",
      force = true
    )
    resTree
  }
}
