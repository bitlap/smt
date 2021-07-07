package io.github.dreamylost.macros

import scala.reflect.macros.whitebox

/**
 *
 * @author 梦境迷离
 * @since 2021/7/7
 * @version 1.0
 */
object synchronizedMacro extends MacroCommon {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    val args: (Boolean, String) = extractArgumentsTuple2(c) {
      case q"new synchronized(verbose=$verbose, lockedName=$lock)" => (evalTree(c)(verbose.asInstanceOf[Tree]), evalTree(c)(lock.asInstanceOf[Tree]))
      case q"new synchronized(lockedName=$lock)" => (false, evalTree(c)(lock.asInstanceOf[Tree]))
      case q"new synchronized()" => (false, "this")
      case _ => c.abort(c.enclosingPosition, ErrorMessage.UNEXPECTED_PATTERN)
    }

    c.info(c.enclosingPosition, s"annottees: $annottees", force = args._1)

    val resTree = annottees map (_.tree) match {
      // Match a method, and expand.
      case _@ q"$modrs def $tname[..$tparams](...$paramss): $tpt = $expr" :: _ =>
        if (args._2 != null) {
          if (args._2 == "this") {
            q"""def $tname[..$tparams](...$paramss): $tpt = ${This(TypeName(""))}.synchronized { $expr }"""
          } else {
            q"""def $tname[..$tparams](...$paramss): $tpt = ${TermName(args._2)}.synchronized { $expr }"""
          }
        } else {
          c.abort(c.enclosingPosition, "Invalid args, lockName cannot be a null!")
        }
      case _ => c.abort(c.enclosingPosition, "Invalid annotation target: not a method")
    }
    printTree(c)(args._1, resTree)
    c.Expr[Any](resTree)
  }
}
