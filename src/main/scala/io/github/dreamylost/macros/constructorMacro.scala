package io.github.dreamylost.macros

import scala.reflect.macros.whitebox

/**
 *
 * @author 梦境迷离
 * @since 2021/7/7
 * @version 1.0
 */
object constructorMacro extends MacroCommon {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    val args = c.prefix.tree match {
      case q"new constructor(verbose=$verbose)" => (c.eval[Boolean](c.Expr(verbose)), Nil)
      case q"new constructor(excludeFields=$excludeFields)" => (false, c.eval[Seq[String]](c.Expr(excludeFields)))
      case q"new constructor(verbose=$verbose, excludeFields=$excludeFields)" => (c.eval[Boolean](c.Expr(verbose)), c.eval[Seq[String]](c.Expr(excludeFields)))
      case q"new constructor()" => (false, Nil)
      case _ => c.abort(c.enclosingPosition, ErrorMessage.UNEXPECTED_PATTERN)
    }

    val annotateeClass: ClassDef = checkAndGetClassDef(c)(annottees: _*)
    val isCase: Boolean = isCaseClass(c)(annotateeClass)
    if (isCase) {
      c.abort(c.enclosingPosition, s"${ErrorMessage.ONLY_CLASS} classDef: $annotateeClass")
    }

    c.info(c.enclosingPosition, s"annottees: $annottees, annotateeClass: $annotateeClass", args._1)

    def modifiedDeclaration(classDecl: ClassDef, compDeclOpt: Option[ModuleDef] = None): Any = {
      val (annotteeClassParams, annotteeClassDefinitions) = classDecl match {
        case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" =>
          c.info(c.enclosingPosition, s"modifiedDeclaration className: $tpname, paramss: $paramss", force = args._1)
          (paramss, stats.asInstanceOf[Seq[Tree]])
        case _ => c.abort(c.enclosingPosition, s"${ErrorMessage.ONLY_CLASS} classDef: $classDecl")
      }

      // Extract the field of the primary constructor.
      val annotteeClassParamsOnlyAssignExpr = fieldAssignExpr(c)(annotteeClassParams.asInstanceOf[List[List[Tree]]].flatten)

      // Extract the internal fields of members belonging to the class， but not in primary constructor.
      val annotteeClassFieldDefinitions = getClassMemberValDef(c)(annotteeClassDefinitions)
      val excludeFields = args._2

      /**
       * Extract the internal fields of members belonging to the class， but not in primary constructor and only `var`.
       */
      def getClassMemberVarDefOnlyAssignExpr(): Seq[c.Tree] = {
        import c.universe._
        getClassMemberValDef(c)(annotteeClassDefinitions).filter(_ match {
          case q"$mods var $tname: $tpt = $expr" if !excludeFields.contains(tname.asInstanceOf[TermName].decodedName.toString) => true
          case _ => false
        }).map {
          case q"$mods var $tname: $tpt = $expr" => q"$tname: $tpt"
        }
      }

      val annotteeClassFieldDefinitionsOnlyAssignExpr = getClassMemberVarDefOnlyAssignExpr()

      if (annotteeClassFieldDefinitionsOnlyAssignExpr.isEmpty) {
        c.abort(c.enclosingPosition, s"Annotation is only supported on class when the internal field (declare as 'var') is nonEmpty. classDef: $classDecl")
      }

      val annotteeClassFieldNames = annotteeClassFieldDefinitions.filter(_ match {
        case q"$mods var $tname: $tpt = $expr" if !excludeFields.contains(tname.asInstanceOf[TermName].decodedName.toString) => true
        case _ => false
      }).map {
        case q"$mods var $tname: $tpt = $expr" => tname.asInstanceOf[TermName]
      }

      c.info(c.enclosingPosition, s"modifiedDeclaration compDeclOpt: $compDeclOpt, annotteeClassParams: $annotteeClassParams", force = args._1)

      // not suppport currying
      val ctorFieldNames = annotteeClassParams.asInstanceOf[List[List[Tree]]].flatten.map(f => fieldTermName(c)(f))

      def getConstructorTemplate(): c.universe.Tree = {
        q"""
          def this(..${annotteeClassParamsOnlyAssignExpr ++ annotteeClassFieldDefinitionsOnlyAssignExpr}){
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
    printTree(c)(force = args._1, resTree.tree)

    resTree

  }
}
