package io.github.liguobin

import scala.annotation.{ StaticAnnotation, compileTimeOnly }
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

/**
 * toString for class
 *
 * @author 梦境迷离
 * @param verbose              Whether to enable detailed log
 * @param isContainsCtorParams Whether to include the fields in the constructor
 * @since 2021/6/13
 * @version 1.0
 */
@compileTimeOnly("enable macro to expand macro annotations")
class toString(verbose: Boolean = false, isContainsCtorParams: Boolean = false) extends StaticAnnotation {

  def macroTransform(annottees: Any*): Any = macro stringMacro.impl

}

object stringMacro {

  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    // extract 'isVerbose' parameters of annotation
    val isVerbose = c.prefix.tree match {
      case Apply(_, q"verbose = $foo" :: Nil) =>
        foo match {
          case Literal(Constant(verbose: Boolean)) => verbose
          case _ =>
            c.warning(
              c.enclosingPosition,
              "The value provided for 'verbose' must be a constant (true or false) and not an expression (e.g. 2 == 1 + 1). Verbose set to false."
            )
            false
        }
      case _ => false
    }

    // extract 'containsCtorParams' parameters of annotation
    val containsCtorParams = c.prefix.tree match {
      case Apply(_, q"isContainsCtorParams = $foo" :: Nil) =>
        foo match {
          case Literal(Constant(isContainsCtorParams: Boolean)) => isContainsCtorParams
          case _ =>
            c.warning(
              c.enclosingPosition,
              "The value provided for 'isContainsCtorParams' must be a constant (true or false) and not an expression (e.g. 2 == 1 + 1). isContainsCtorParams set to false."
            )
            false
        }
      case _ => false
    }

    // Check the type of the class, which can only be defined on the ordinary class
    val annotateeClass: ClassDef = annottees.map(_.tree).toList match {
      case (claz: ClassDef) :: Nil => claz
      case _                       => c.abort(c.enclosingPosition, "Unexpected annottee. Only applicable to class definitions.")
    }

    //    if (annotateeClass.tpe.typeSymbol.asClass.isCaseClass) {
    //      c.warning(
    //        c.enclosingPosition,
    //        "'toString' annotation is used on 'case class'. Ignore"
    //      )
    //    }

    // For a given class definition, separate the components of the class
    val (isCase, className, annotteeClassParams, annotteeClassDefinitions) = {
      annotateeClass match {
        case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" =>
          val isCase = if (mods.asInstanceOf[Modifiers].hasFlag(Flag.CASE)) {
            c.warning(
              c.enclosingPosition,
              "'toString' annotation is used on 'case class'. Ignore")
            true
          } else false
          (isCase, tpname, paramss, stats)
      }
    }

    // Check the type of the class, whether it already contains its own toString
    annotteeClassDefinitions.asInstanceOf[List[Tree]].map {
      case v: ValDef    => true
      case m: MemberDef => c.abort(m.pos, "'toString' method has already defined, please remove it or not use'@toString'")
    }

    // Extract the fields in the class definition
    val fields = annotteeClassDefinitions.asInstanceOf[List[Tree]].map {
      case v: ValDef => v.name
    }

    // For the parameters of a given constructor, separate the parameter components and extract the constructor parameters containing val and var
    val ctorParams = annotteeClassParams.asInstanceOf[List[List[Tree]]].flatten.map {
      case tree @ q"$mods val $tname: $tpt = $expr" => TermName.apply(tname.toString())
      case tree @ q"$mods var $tname: $tpt = $expr" => TermName.apply(tname.toString())
    }

    val member = if (containsCtorParams) ctorParams ++ fields else fields

    // Generate toString method TODO refactor code
    val method =
      q"""
         override def toString(): String =  ($member).toString.replace("List", ${className.toString()})
         """
    val resTree = annotateeClass match {
      case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" =>
        if (!isCase) {
          q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..${stats.toList.:+(method)} }"
        } else {
          q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }"
        }
    }
    // Print the ast
    c.info(
      c.enclosingPosition,
      "\n###### Expanded macro ######\n" + resTree.toString() + "\n###### Expanded macro ######\n",
      force = isVerbose
    )
    c.Expr[Any](resTree)
  }
}
