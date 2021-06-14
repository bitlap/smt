package io.github.liguobin

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

/**
 * toString for class
 *
 * @author 梦境迷离
 * @param verbose            Whether to enable detailed log.
 * @param containsCtorParams Whether to include the fields of the primary constructor.
 * @param withFieldName      Whether to include the name of the field in the toString.
 * @since 2021/6/13
 * @version 1.0
 */
@compileTimeOnly("enable macro to expand macro annotations")
class toString(
                verbose: Boolean = false,
                containsCtorParams: Boolean = true,
                withFieldName: Boolean = true
              ) extends StaticAnnotation {

  def macroTransform(annottees: Any*): Any = macro stringMacro.impl

}

case class Argument(verbose: Boolean, containsCtorParams: Boolean, withFieldName: Boolean)

object stringMacro {

  def printField(c: whitebox.Context)(argument: Argument, lastParam: Option[String], field: c.universe.Tree): c.universe.Tree = {
    import c.universe._
    // Print one field as <name of the field>+"="+fieldName
    if (argument.withFieldName) {
      lastParam.fold(q"$field") { lp =>
        field match {
          case tree@q"$mods var $tname: $tpt = $expr" =>
            if (tname.toString() != lp) q"""${tname.toString()}+${"="}+this.$tname+${", "}""" else q"""${tname.toString()}+${"="}+this.$tname"""
          case tree@q"$mods val $tname: $tpt = $expr" =>
            if (tname.toString() != lp) q"""${tname.toString()}+${"="}+this.$tname+${", "}""" else q"""${tname.toString()}+${"="}+this.$tname"""
          case _ => q"$field"
        }
      }
    } else {
      lastParam.fold(q"$field") { lp =>
        field match {
          case tree@q"$mods var $tname: $tpt = $expr" => if (tname.toString() != lp) q"""$tname+${", "}""" else q"""$tname"""
          case tree@q"$mods val $tname: $tpt = $expr" => if (tname.toString() != lp) q"""$tname+${", "}""" else q"""$tname"""
          case _ => if (field.toString() != lp) q"""$field+${", "}""" else q"""$field"""
        }
      }

    }
  }

  private def toStringTemplateImpl(c: whitebox.Context)(argument: Argument, annotateeClass: c.universe.ClassDef): c.universe.Tree = {
    import c.universe._
    // For a given class definition, separate the components of the class
    val (className, annotteeClassParams, annotteeClassDefinitions) = {
      annotateeClass match {
        case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" =>
          (tpname, paramss, stats)
      }
    }
    // Check the type of the class, whether it already contains its own toString
    val annotteeClassFieldDefinitions = annotteeClassDefinitions.asInstanceOf[List[Tree]].filter(p => p match {
      case _: ValDef => true
      case mem: MemberDef =>
        c.info(c.enclosingPosition, s"MemberDef:  ${mem.toString}", true)
        if (mem.toString().startsWith("override def toString")) {
          c.abort(mem.pos, "'toString' method has already defined, please remove it or not use'@toString'")
        }
        false
      case m: DefDef =>
        false
      case _ => false
    })

    // For the parameters of a given constructor, separate the parameter components and extract the constructor parameters containing val and var
    val ctorParams = annotteeClassParams.asInstanceOf[List[List[Tree]]].flatten.map {
      case tree@q"$mods val $tname: $tpt = $expr" => tree
      case tree@q"$mods var $tname: $tpt = $expr" => tree
    }
    c.info(c.enclosingPosition, s"className： $className, ctorParams: ${ctorParams.toString()}", force = true)
    c.info(c.enclosingPosition, s"className： $className, fields: ${annotteeClassFieldDefinitions.toString()}", force = true)
    val member = if (argument.containsCtorParams) ctorParams ++ annotteeClassFieldDefinitions else annotteeClassFieldDefinitions

    val lastParam = member.lastOption.map {
      case v: ValDef => v.name.toTermName.decodedName.toString
      case c => c.toString
    }
    val paramsWithName = member.foldLeft(q"${""}")((res, acc) => q"$res + ${printField(c)(argument, lastParam, acc)}")
    //scala/bug https://github.com/scala/bug/issues/3967 not be 'Foo(i=1,j=2)' in standard library
    q"""override def toString: String = ${className.toString()} + ${"("} + $paramsWithName + ${")"}"""
  }

  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    // extract parameters of annotation
    // extract 'isVerbose' parameters of annotation
    val arg = c.prefix.tree match {
      case q"new toString($aa, $bb, $cc)" => (c.eval[Boolean](c.Expr(aa)), c.eval[Boolean](c.Expr(bb)), c.eval[Boolean](c.Expr(cc)))
      case _ => c.abort(c.enclosingPosition, "unexpected annotation pattern!")
    }
    val argument = Argument(arg._1, arg._2, arg._3)
    // Check the type of the class, which can only be defined on the ordinary class
    val annotateeClass: ClassDef = annottees.map(_.tree).toList match {
      case (claz: ClassDef) :: Nil => claz
      case _ => c.abort(c.enclosingPosition, "Unexpected annottee. Only applicable to class definitions.")
    }
    val isCase: Boolean = {
      annotateeClass match {
        case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" =>
          if (mods.asInstanceOf[Modifiers].hasFlag(Flag.CASE)) {
            c.warning(c.enclosingPosition, "'toString' annotation is used on 'case class'.")
            true
          } else false
      }
    }

    c.info(c.enclosingPosition, s"impl argument: $argument, isCase: $isCase", true)
    val resMethod = toStringTemplateImpl(c)(argument, annotateeClass)
    val resTree = annotateeClass match {
      case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" =>
        q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..${stats.toList.:+(resMethod)} }"
    }
    // Print the ast
    c.info(
      c.enclosingPosition,
      "\n###### Expanded macro ######\n" + resTree.toString() + "\n###### Expanded macro ######\n",
      force = argument.verbose
    )
    c.Expr[Any](resTree)
  }
}
