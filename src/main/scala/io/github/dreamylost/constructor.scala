package io.github.dreamylost

import scala.annotation.{ StaticAnnotation, compileTimeOnly }
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

/**
 * annotation to generate secondary constructor method for classes.
 *
 * @author 梦境迷离
 * @param verbose Whether to enable detailed log.
 * @since 2021/7/3
 * @version 1.0
 */
@compileTimeOnly("enable macro to expand macro annotations")
final class constructor(
    verbose: Boolean = false
) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro constructorMacro.impl

}

object constructorMacro extends MacroCommon {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    val args: Boolean = c.prefix.tree match {
      case q"new constructor(verbose=$verbose)" => c.eval[Boolean](c.Expr(verbose))
      case q"new constructor()"                 => false
      case _                                    => c.abort(c.enclosingPosition, "unexpected annotation pattern!")
    }

    val annotateeClass: ClassDef = checkAndReturnClass(c)(annottees: _*)
    val isCase: Boolean = isCaseClass(c)(annotateeClass)
    if (isCase) {
      c.abort(c.enclosingPosition, s"Annotation is not supported on case class. classDef: $annotateeClass")
    }

    c.info(c.enclosingPosition, s"annottees: $annottees, annotateeClass: $annotateeClass", args)

    def modifiedDeclaration(classDecl: ClassDef, compDeclOpt: Option[ModuleDef] = None): Any = {
      val (annotteeClassParams, annotteeClassDefinitions) = classDecl match {
        case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" =>
          c.info(c.enclosingPosition, s"modifiedDeclaration className: $tpname, paramss: $paramss", force = args)
          (paramss, stats)
        case _ => c.abort(c.enclosingPosition, s"Annotation is only supported on class. classDef: $classDecl")
      }

      // Extract the field of the primary constructor.
      val annotteeClassParamsWithoutValAssign = annotteeClassParams.asInstanceOf[List[List[Tree]]].flatten.map {
        case q"$mods var $tname: $tpt = $expr" => q"$tname: $tpt"
        case q"$mods val $tname: $tpt = $expr" => q"$tname: $tpt"
      }

      // Extract the internal fields of members belonging to the class， but not in primary constructor.
      val annotteeClassFieldDefinitions = getClassMemberValDef(c)(annotteeClassDefinitions.asInstanceOf[Seq[Tree]])

      val annotteeClassFieldDefinitionsWithoutValAssign = getClassMemberValDefOnlyVarAssign(c)(annotteeClassDefinitions.asInstanceOf[Seq[Tree]])

      if (annotteeClassFieldDefinitionsWithoutValAssign.isEmpty) {
        c.abort(c.enclosingPosition, s"Annotation is only supported on class when the internal field (declare as 'var') is nonEmpty. classDef: $classDecl")
      }

      val annotteeClassFieldNames = annotteeClassFieldDefinitions.filter(_ match {
        case q"$mods var $tname: $tpt = $expr" => true
        case _                                 => false
      }).map {
        case q"$mods var $tname: $tpt = $expr" => tname.asInstanceOf[TermName]
      }

      c.info(c.enclosingPosition, s"modifiedDeclaration compDeclOpt: $compDeclOpt, annotteeClassParams: $annotteeClassParams", force = args)

      // not suppport currying
      val ctorFieldNames = annotteeClassParams.asInstanceOf[List[List[Tree]]].flatten.map(f => fieldTermNameMethod(c)(f))

      def getConstructorTemplate(): c.universe.Tree = {
        q"""
          def this(..${annotteeClassParamsWithoutValAssign ++ annotteeClassFieldDefinitionsWithoutValAssign}){
            this(..$ctorFieldNames)
            ..${annotteeClassFieldNames.map(f => q"this.$f = $f")}
          }
         """
      }

      val resTree = annotateeClass match {
        case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" =>
          q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..${stats.toList.:+(getConstructorTemplate())} }"
      }
      c.Expr[Any](treeResultWithCompanionObject(c)(resTree, annottees: _*))
    }

    val resTree = handleWithImplType(c)(annottees: _*)(modifiedDeclaration)
    printTree(c)(force = args, resTree.tree)

    resTree

  }
}
