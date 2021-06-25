package io.github.dreamylost

import scala.annotation.{ StaticAnnotation, compileTimeOnly }
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

/**
 *
 * @author 梦境迷离
 * @param lockedName The name of custom lock obj.
 * @param verbose    Whether to enable detailed log.
 * @since 2021/6/24
 * @version 1.0
 */
@compileTimeOnly("enable macro to expand macro annotations")
final class synchronized(
    verbose:    Boolean = false,
    lockedName: String  = "this"
) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro synchronizedMacro.impl
}

object synchronizedMacro {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    val args = c.prefix.tree match {
      case q"new synchronized(verbose=$verbose, lockedName=$lock)" => (c.eval[Boolean](c.Expr(verbose)), c.eval[String](c.Expr(lock)))
      case q"new synchronized(lockedName=$lock)" => (false, c.eval[String](c.Expr(lock)))
      case q"new synchronized()" => (false, "this")
      case _ => c.abort(c.enclosingPosition, "unexpected annotation pattern!")

    }
    c.info(c.enclosingPosition, s"annottees: $annottees", force = args._1)

    val resTree = annottees map (_.tree) match {
      // Match a method, and expand.
      case m @ q"$modrs def $tname[..$tparams](...$paramss): $tpt = $expr" :: _ =>
        val mod = modrs match {
          case m: Modifiers => Some(m.annotations)
          case _            => None
        }
        c.info(c.enclosingPosition, s"modifiers: $modrs, mod :$mod", force = args._1)
        mod.fold(q"$modrs def $tname[..$tparams](...$paramss): $tpt = $expr") { _ =>
          if (args._2 != null) {
            if (args._2 == "this") {
              q"""def $tname[..$tparams](...$paramss): $tpt = ${This(TypeName(""))}.synchronized { $expr }"""
            } else {
              q"""def $tname[..$tparams](...$paramss): $tpt = ${TermName(args._2)}.synchronized { $expr }"""
            }
          } else {
            c.abort(c.enclosingPosition, "Invalid args, lockName cannot be null!")
          }
        }
      case _ => c.abort(c.enclosingPosition, "Invalid annotation target: not a method")
    }
    // Print the ast
    c.info(
      c.enclosingPosition,
      "\n###### Expanded macro ######\n" + resTree.toString() + "\n###### Expanded macro ######\n",
      force = args._1
    )
    c.Expr[Any](resTree)
  }
}
