package io.github.dreamylost.macros

import io.github.dreamylost.logs.LogType._
import io.github.dreamylost.logs._

import scala.reflect.macros.whitebox

/**
 *
 * @author 梦境迷离
 * @since 2021/7/7
 * @version 1.0
 */
object logMacro extends MacroCommon {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    val args: (Boolean, LogType) = c.prefix.tree match {
      case q"new log(logType=$logType)" => (false, c.eval[LogType](c.Expr(logType)))
      case q"new log(verbose=$verbose)" => (c.eval[Boolean](c.Expr(verbose)), LogType.JLog)
      case q"new log($logType)" => (false, c.eval[LogType](c.Expr(logType)))
      case q"new log(verbose=$verbose, logType=$logType)" => (c.eval[Boolean](c.Expr(verbose)), c.eval[LogType](c.Expr(logType)))
      case q"new log()" => (false, LogType.JLog)
      case _ => c.abort(c.enclosingPosition, "unexpected annotation pattern!")
    }

    c.info(c.enclosingPosition, s"annottees: $annottees, args: $args", force = args._1)

    val logTree = annottees.map(_.tree) match {
      // Match a class, and expand, get class/object name.
      case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" :: _ =>
        LogType.getLogImpl(args._2).getTemplate(c)(tpname.asInstanceOf[TypeName].toTermName.decodedName.toString, isClass = true)
      case q"$mods object $tpname extends { ..$earlydefns } with ..$parents { $self => ..$stats }" :: _ =>
        LogType.getLogImpl(args._2).getTemplate(c)(tpname.asInstanceOf[TermName].decodedName.toString, isClass = false)
      case _ => c.abort(c.enclosingPosition, s"Annotation is only supported on class or object.")
    }

    // add result into class
    val resTree = annottees.map(_.tree) match {
      case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" :: _ =>
        val resTree = q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..${List(logTree) ::: stats.toList} }"
        treeResultWithCompanionObject(c)(resTree, annottees: _*) //we should return with companion object. Even if we didn't change it.
      case q"$mods object $tpname extends { ..$earlydefns } with ..$parents { $self => ..$stats }" :: _ =>
        q"$mods object $tpname extends { ..$earlydefns } with ..$parents { $self => ..${List(logTree) ::: stats.toList} }"
      //we should return with class def. Even if we didn't change it, but the context class was not passed in.
      //        val annotateeClassOpt: Option[ClassDef] = getClassDef(c)(annottees: _*)
      //        if(annotateeClassOpt.isEmpty){
      //          resTree
      //        } else {
      //          q"""
      //              ${annotateeClassOpt.get}
      //              $resTree
      //            """
      //        }
    }

    printTree(c)(force = args._1, resTree)
    c.Expr[Any](resTree)
  }
}
