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
    val args: Tuple1[Boolean] = extractArgumentsTuple1(c) {
      case q"new apply(verbose=$verbose)" => Tuple1(evalTree(c)(verbose.asInstanceOf[Tree]))
      case q"new apply()"                 => Tuple1(false)
      case _                              => c.abort(c.enclosingPosition, ErrorMessage.UNEXPECTED_PATTERN)
    }

    c.info(c.enclosingPosition, s"annottees: $annottees, args: $args", force = args._1)

    val annotateeClass: ClassDef = checkAndGetClassDef(c)(annottees: _*)
    val isCase: Boolean = isCaseClass(c)(annotateeClass)
    c.info(c.enclosingPosition, s"impl argument: $args, isCase: $isCase", force = args._1)

    if (isCase) c.abort(c.enclosingPosition, s"Annotation is only supported on 'case class'")

    def modifiedDeclaration(classDecl: ClassDef, compDeclOpt: Option[ModuleDef] = None): Any = {
      val (className, classParams, classTypeParams) = classDecl match {
        case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends ..$bases { ..$body }" =>
          c.info(c.enclosingPosition, s"modifiedDeclaration className: $tpname, paramss: $paramss", force = args._1)
          (tpname, paramss.asInstanceOf[List[List[Tree]]], tparams)
        case _ => c.abort(c.enclosingPosition, s"${ErrorMessage.ONLY_CLASS} classDef: $classDecl")
      }
      c.info(c.enclosingPosition, s"modifiedDeclaration compDeclOpt: $compDeclOpt, annotteeClassParams: $classParams", force = args._1)
      val tpName = className match {
        case t: TypeName => t
      }
      val apply = getApplyMethodWithCurrying(c)(tpName, classParams, classTypeParams)
      val compDecl = modifiedCompanion(c)(compDeclOpt, apply, tpName)
      c.Expr(
        q"""
            $classDecl
            $compDecl
          """)
    }

    val resTree = handleWithImplType(c)(annottees: _*)(modifiedDeclaration)
    printTree(c)(force = args._1, resTree.tree)

    resTree

  }
}
