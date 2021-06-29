package io.github.dreamylost

import io.github.dreamylost.LogType.LogType

import scala.annotation.{ StaticAnnotation, compileTimeOnly }
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

/**
 *
 * @author 梦境迷离
 * @param verbose Whether to enable detailed log.
 * @param logType Specifies the type of `log` that needs to be generated
 * @since 2021/6/28
 * @version 1.0
 */
@compileTimeOnly("enable macro to expand macro annotations")
final class log(
    verbose: Boolean         = false,
    logType: LogType.LogType = LogType.JLog
) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro logMacro.impl
}

sealed trait BaseLog {
  val typ: LogType

  def getTemplate(c: whitebox.Context)(t: String, isClass: Boolean): c.Tree
}

object LogType extends Enumeration {
  type LogType = Value
  val JLog, Log4j2, Slf4j = Value

  private lazy val types = Map(
    JLog -> JBaseLogImpl,
    Log4j2 -> Log4J2Impl,
    Slf4j -> Slf4jImpl
  )

  def getLogImpl(logType: LogType): BaseLog = {
    types.getOrElse(logType, default = throw new Exception(s"Not support log: $logType"))
  }

}

object JBaseLogImpl extends BaseLog {
  override val typ: LogType = LogType.JLog

  override def getTemplate(c: whitebox.Context)(t: String, isClass: Boolean): c.Tree = {
    import c.universe._
    if (isClass) {
      q"""private val log: java.util.logging.Logger = java.util.logging.Logger.getLogger(classOf[${TypeName(t)}].getName)"""
    } else {
      q"""private val log: java.util.logging.Logger = java.util.logging.Logger.getLogger(${TermName(t)}.getClass.getName)"""
    }
  }

}

object Log4J2Impl extends BaseLog {
  override val typ: LogType = LogType.Log4j2

  override def getTemplate(c: whitebox.Context)(t: String, isClass: Boolean): c.Tree = ???
}

object Slf4jImpl extends BaseLog {
  override val typ: LogType = LogType.Slf4j

  override def getTemplate(c: whitebox.Context)(t: String, isClass: Boolean): c.Tree = ???
}

object logMacro extends MacroCommon {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    val args: (Boolean, LogType) = c.prefix.tree match {
      case q"new log(logType=$logType)" => (false, c.eval[LogType](c.Expr(logType)))
      case q"new log(verbose=$verbose)" => (c.eval[Boolean](c.Expr(verbose)), LogType.JLog)
      case q"new log(verbose=$verbose, logType=$logType)" => (c.eval[Boolean](c.Expr(verbose)), c.eval[LogType](c.Expr(logType)))
      case q"new log()" => (false, LogType.JLog)
      case _ => c.abort(c.enclosingPosition, "unexpected annotation pattern!")
    }

    c.info(c.enclosingPosition, s"annottees: $annottees, args: $args", force = args._1)

    val logTree = annottees.map(_.tree) match {
      // Match a class, and expand, get class/object name.
      case (classDef @ q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }") :: _ =>
        LogType.getLogImpl(args._2).getTemplate(c)(tpname.asInstanceOf[TypeName].toTermName.decodedName.toString, isClass = true)
      case (classDef @ q"$mods object $tpname extends { ..$earlydefns } with ..$parents { $self => ..$stats }") :: _ =>
        LogType.getLogImpl(args._2).getTemplate(c)(tpname.asInstanceOf[TermName].decodedName.toString, isClass = false)
      case _ => c.abort(c.enclosingPosition, s"Annotation is only supported on class or object.")

    }

    // add result into class
    val resTree = annottees.map(_.tree) match {
      case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" :: _ =>
        q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..${List(logTree) ::: stats.toList} }"
      case q"$mods object $tpname extends { ..$earlydefns } with ..$parents { $self => ..$stats }" :: _ =>
        q"$mods object $tpname extends { ..$earlydefns } with ..$parents { $self => ..${List(logTree) ::: stats.toList} }"
    }

    printTree(c)(force = args._1, resTree)
    c.Expr[Any](resTree)
  }
}
