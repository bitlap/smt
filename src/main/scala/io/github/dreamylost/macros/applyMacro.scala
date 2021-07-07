package io.github.dreamylost.macros

import scala.reflect.macros.whitebox

/**
 *
 * @author 梦境迷离
 * @since 2021/7/7
 * @version 1.0
 */
object applyMacro extends MacroCommon {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    val args: Boolean = c.prefix.tree match {
      case q"new apply(verbose=$verbose)" => c.eval[Boolean](c.Expr(verbose))
      case q"new apply()"                 => false
      case _                              => c.abort(c.enclosingPosition, ErrorMessage.UNEXPECTED_PATTERN)
    }

    c.info(c.enclosingPosition, s"annottees: $annottees, args: $args", force = args)

    val annotateeClass: ClassDef = checkAndGetClassDef(c)(annottees: _*)
    val isCase: Boolean = isCaseClass(c)(annotateeClass)
    c.info(c.enclosingPosition, s"impl argument: $args, isCase: $isCase", force = args)

    def modifiedDeclaration(classDecl: ClassDef, compDeclOpt: Option[ModuleDef] = None): Any = {
      val (className, annotteeClassParams) = classDecl match {
        case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends ..$bases { ..$body }" =>
          c.info(c.enclosingPosition, s"modifiedDeclaration className: $tpname, paramss: $paramss", force = args)
          (tpname, paramss)
        case _ => c.abort(c.enclosingPosition, s"${ErrorMessage.ONLY_CLASS} classDef: $classDecl")
      }
      c.info(c.enclosingPosition, s"modifiedDeclaration compDeclOpt: $compDeclOpt, annotteeClassParams: $annotteeClassParams", force = args)
      val fieldNames = annotteeClassParams.asInstanceOf[List[List[Tree]]].flatten.map(f => fieldTermName(c)(f))
      val cName = className match {
        case t: TypeName => t
      }
      val annotteeClassParamsOnlyAssignExpr = fieldAssignExpr(c)(annotteeClassParams.asInstanceOf[List[List[Tree]]].flatten)
      val compDecl = modifiedCompanion(c)(compDeclOpt, q"""def apply(..$annotteeClassParamsOnlyAssignExpr): $className = new $className(..$fieldNames)""", cName)
      c.Expr(
        q"""
            $classDecl
            $compDecl
          """)
    }

    val resTree = handleWithImplType(c)(annottees: _*)(modifiedDeclaration)
    printTree(c)(force = args, resTree.tree)

    resTree

  }
}
