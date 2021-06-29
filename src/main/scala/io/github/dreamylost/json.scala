package io.github.dreamylost

import scala.annotation.{ StaticAnnotation, compileTimeOnly }
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

/**
 * annotation for case classes
 *
 * @author 梦境迷离
 * @since 2021/6/13
 * @version 1.0
 */
@compileTimeOnly("enable macro to expand macro annotations")
final class json extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro jsonMacro.impl
}

object jsonMacro extends MacroCommon {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def jsonFormatter(className: TypeName, fields: List[Tree]): c.universe.Tree = {
      fields.length match {
        case 0 => c.abort(c.enclosingPosition, "Cannot create json formatter for case class with no fields")
        case _ =>
          c.info(c.enclosingPosition, s"jsonFormatter className: $className, field length: ${fields.length}", force = true)
          q"implicit val jsonAnnotationFormat = play.api.libs.json.Json.format[$className]"
      }
    }

    def modifiedCompanion(compDeclOpt: Option[ModuleDef], format: Tree, className: TypeName): c.universe.Tree = {
      compDeclOpt map { compDecl =>
        // Add the formatter to the existing companion object
        val q"object $obj extends ..$bases { ..$body }" = compDecl
        val o =
          q"""
          object $obj extends ..$bases {
            ..$body
            $format
          }
        """
        c.info(c.enclosingPosition, s"modifiedCompanion className: $className, exists obj: $o", force = true)
        o
      } getOrElse {
        // Create a companion object with the formatter
        val o = q"object ${className.toTermName} { $format }"
        c.info(c.enclosingPosition, s"modifiedCompanion className: $className, new obj: $o", force = true)
        o
      }
    }

    // The dependent type need aux-pattern in scala2. Now let's get around this.
    def modifiedDeclaration(classDecl: ClassDef, compDeclOpt: Option[ModuleDef] = None): Any = {
      val (className, fields) = classDecl match {
        case q"$mods class $className(..$fields) extends ..$bases { ..$body }" =>
          if (!mods.asInstanceOf[Modifiers].hasFlag(Flag.CASE)) {
            c.abort(c.enclosingPosition, s"Annotation is only supported on case class. classDef: $classDecl, mods: $mods")
          } else {
            c.info(c.enclosingPosition, s"modifiedDeclaration className: $className, fields: $fields", force = true)
            (className, fields)
          }
        case _ => c.abort(c.enclosingPosition, s"Annotation is only supported on case class. classDef: $classDecl")
      }
      c.info(c.enclosingPosition, s"modifiedDeclaration className: $className, fields: $fields", force = true)
      className match {
        case t: TypeName =>
          val format = jsonFormatter(t, fields.asInstanceOf[List[Tree]])
          val compDecl = modifiedCompanion(compDeclOpt, format, t)
          c.info(c.enclosingPosition, s"format: $format, compDecl: $compDecl", force = true)
          // Return both the class and companion object declarations
          c.Expr(
            q"""
        $classDecl
        $compDecl
      """)
      }

    }

    c.info(c.enclosingPosition, s"json annottees: $annottees", force = true)
    val resTree = handleWithImplType(c)(annottees: _*)(modifiedDeclaration)
    printTree(c)(force = true, resTree.tree)

    resTree
  }
}
