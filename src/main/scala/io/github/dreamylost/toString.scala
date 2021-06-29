package io.github.dreamylost

import scala.annotation.{ StaticAnnotation, compileTimeOnly }
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

/**
 * toString for classes
 *
 * @author 梦境迷离
 * @param verbose               Whether to enable detailed log.
 * @param includeInternalFields Whether to include the fields defined within a class.
 * @param includeFieldNames     Whether to include the name of the field in the toString.
 * @param callSuper             Whether to include the super's toString.
 * @since 2021/6/13
 * @version 1.0
 */
@compileTimeOnly("enable macro to expand macro annotations")
final class toString(
    verbose:               Boolean = false,
    includeInternalFields: Boolean = true,
    includeFieldNames:     Boolean = true,
    callSuper:             Boolean = false
) extends StaticAnnotation {

  def macroTransform(annottees: Any*): Any = macro stringMacro.impl

}

final case class Argument(verbose: Boolean, includeInternalFields: Boolean, includeFieldNames: Boolean, callSuper: Boolean)

object stringMacro extends MacroCommon {

  def printField(c: whitebox.Context)(argument: Argument, lastParam: Option[String], field: c.universe.Tree): c.universe.Tree = {
    import c.universe._
    // Print one field as <name of the field>+"="+fieldName
    if (argument.includeFieldNames) {
      lastParam.fold(q"$field") { lp =>
        field match {
          case tree @ q"$mods var $tname: $tpt = $expr" =>
            if (tname.toString() != lp) q"""${tname.toString()}+${"="}+this.$tname+${", "}""" else q"""${tname.toString()}+${"="}+this.$tname"""
          case tree @ q"$mods val $tname: $tpt = $expr" =>
            if (tname.toString() != lp) q"""${tname.toString()}+${"="}+this.$tname+${", "}""" else q"""${tname.toString()}+${"="}+this.$tname"""
          case _ => q"$field"
        }
      }
    } else {
      lastParam.fold(q"$field") { lp =>
        field match {
          case tree @ q"$mods var $tname: $tpt = $expr" => if (tname.toString() != lp) q"""$tname+${", "}""" else q"""$tname"""
          case tree @ q"$mods val $tname: $tpt = $expr" => if (tname.toString() != lp) q"""$tname+${", "}""" else q"""$tname"""
          case _                                        => if (field.toString() != lp) q"""$field+${", "}""" else q"""$field"""
        }
      }

    }
  }

  private def toStringTemplateImpl(c: whitebox.Context)(argument: Argument, annotateeClass: c.universe.ClassDef): c.universe.Tree = {
    import c.universe._
    // For a given class definition, separate the components of the class
    val (className, annotteeClassParams, superClasses, annotteeClassDefinitions) = {
      annotateeClass match {
        case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" =>
          c.info(c.enclosingPosition, s"parents: $parents", force = argument.verbose)
          (tpname, paramss, parents, stats)
      }
    }
    // Check the type of the class, whether it already contains its own toString
    val annotteeClassFieldDefinitions = annotteeClassDefinitions.asInstanceOf[List[Tree]].filter(p => p match {
      case _: ValDef => true
      case mem: MemberDef =>
        c.info(c.enclosingPosition, s"MemberDef:  ${mem.toString}", force = argument.verbose)
        if (mem.toString().startsWith("override def toString")) { // TODO better way
          c.abort(mem.pos, "'toString' method has already defined, please remove it or not use'@toString'")
        }
        false
      case _ => false
    })

    // For the parameters of a given constructor, separate the parameter components and extract the constructor parameters containing val and var
    val ctorParams = annotteeClassParams.asInstanceOf[List[List[Tree]]].flatten.map {
      case tree @ q"$mods val $tname: $tpt = $expr" => tree
      case tree @ q"$mods var $tname: $tpt = $expr" => tree
    }
    c.info(c.enclosingPosition, s"className： $className, ctorParams: ${ctorParams.toString()}, superClasses: $superClasses", force = argument.verbose)
    c.info(c.enclosingPosition, s"className： $className, fields: ${annotteeClassFieldDefinitions.toString()}", force = argument.verbose)
    val member = if (argument.includeInternalFields) ctorParams ++ annotteeClassFieldDefinitions else ctorParams

    val lastParam = member.lastOption.map {
      case v: ValDef => v.name.toTermName.decodedName.toString
      case c         => c.toString
    }
    val paramsWithName = member.foldLeft(q"${""}")((res, acc) => q"$res + ${printField(c)(argument, lastParam, acc)}")
    //scala/bug https://github.com/scala/bug/issues/3967 not be 'Foo(i=1,j=2)' in standard library
    val toString = q"""override def toString: String = ${className.toString()} + ${"("} + $paramsWithName + ${")"}"""

    // Have super class ?
    if (argument.callSuper && superClasses.nonEmpty) {
      val superClassDef = superClasses.head match {
        case tree: Tree => Some(tree) // TODO type check better
        case _          => None
      }
      superClassDef.fold(toString)(sc => {
        val superClass = q"${"super="}"
        c.info(c.enclosingPosition, s"member: $member, superClass： $superClass, superClassDef: $superClassDef, paramsWithName: $paramsWithName", force = argument.verbose)
        q"override def toString: String = StringContext(${className.toString()} + ${"("} + $superClass, ${if (member.nonEmpty) ", " else ""}+$paramsWithName + ${")"}).s(super.toString)"
      }
      )
    } else {
      toString
    }

  }

  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    // extract parameters of annotation, must in order
    val arg = c.prefix.tree match {
      case q"new toString(includeInternalFields=$bb, includeFieldNames=$cc, callSuper=$dd)" => (false, c.eval[Boolean](c.Expr(bb)), c.eval[Boolean](c.Expr(cc)), c.eval[Boolean](c.Expr(dd)))
      case q"new toString($aa, $bb, $cc)" => (c.eval[Boolean](c.Expr(aa)), c.eval[Boolean](c.Expr(bb)), c.eval[Boolean](c.Expr(cc)), false)

      case q"new toString(verbose=$aa, includeInternalFields=$bb, includeFieldNames=$cc, callSuper=$dd)" => (c.eval[Boolean](c.Expr(aa)), c.eval[Boolean](c.Expr(bb)), c.eval[Boolean](c.Expr(cc)), c.eval[Boolean](c.Expr(dd)))
      case q"new toString(verbose=$aa, includeInternalFields=$bb, includeFieldNames=$cc)" => (c.eval[Boolean](c.Expr(aa)), c.eval[Boolean](c.Expr(bb)), c.eval[Boolean](c.Expr(cc)), false)
      case q"new toString($aa, $bb, $cc, $dd)" => (c.eval[Boolean](c.Expr(aa)), c.eval[Boolean](c.Expr(bb)), c.eval[Boolean](c.Expr(cc)), c.eval[Boolean](c.Expr(dd)))

      case q"new toString(includeInternalFields=$bb, includeFieldNames=$cc)" => (false, c.eval[Boolean](c.Expr(bb)), c.eval[Boolean](c.Expr(cc)), false)
      case q"new toString()" => (false, true, true, false)
      case _ => c.abort(c.enclosingPosition, "unexpected annotation pattern!")
    }
    val argument = Argument(arg._1, arg._2, arg._3, arg._4)
    c.info(c.enclosingPosition, s"toString annottees: $annottees", force = argument.verbose)
    // Check the type of the class, which can only be defined on the ordinary class
    val annotateeClass: ClassDef = checkAndReturnClass(c)(annottees: _*)
    val isCase: Boolean = isCaseClass(c)(annotateeClass)

    c.info(c.enclosingPosition, s"impl argument: $argument, isCase: $isCase", force = argument.verbose)
    val resMethod = toStringTemplateImpl(c)(argument, annotateeClass)
    val resTree = annotateeClass match {
      case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" =>
        q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..${stats.toList.:+(resMethod)} }"
    }
    printTree(c)(argument.verbose, resTree)
    c.Expr[Any](resTree)
  }
}
