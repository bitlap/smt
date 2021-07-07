package io.github.dreamylost.macros

import scala.reflect.macros.whitebox

/**
 *
 * @author 梦境迷离
 * @since 2021/7/7
 * @version 1.0
 */
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

    // The dependent type need aux-pattern in scala2. Now let's get around this.
    def modifiedDeclaration(classDecl: ClassDef, compDeclOpt: Option[ModuleDef] = None): Any = {
      val (className, fields) = classDecl match {
        case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends ..$bases { ..$body }" =>
          if (!mods.asInstanceOf[Modifiers].hasFlag(Flag.CASE)) {
            c.abort(c.enclosingPosition, s"Annotation is only supported on case class. classDef: $classDecl, mods: $mods")
          } else {
            c.info(c.enclosingPosition, s"modifiedDeclaration className: $tpname, paramss: $paramss", force = true)
            (tpname, paramss)
          }
        case _ => c.abort(c.enclosingPosition, s"${ErrorMessage.ONLY_CLASS} classDef: $classDecl")
      }
      c.info(c.enclosingPosition, s"modifiedDeclaration className: $className, fields: $fields", force = true)
      val cName = className match {
        case t: TypeName => t
      }
      val format = jsonFormatter(cName, fields.asInstanceOf[List[List[Tree]]].flatten)
      val compDecl = modifiedCompanion(c)(compDeclOpt, format, cName)
      c.info(c.enclosingPosition, s"format: $format, compDecl: $compDecl", force = true)
      // Return both the class and companion object declarations
      c.Expr(
        q"""
        $classDecl
        $compDecl
      """)

    }

    c.info(c.enclosingPosition, s"json annottees: $annottees", force = true)
    val resTree = handleWithImplType(c)(annottees: _*)(modifiedDeclaration)
    printTree(c)(force = true, resTree.tree)

    resTree
  }
}
