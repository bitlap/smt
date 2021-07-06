package io.github.dreamylost.logs

import io.github.dreamylost.logs.LogType.LogType

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

object JLogImpl extends BaseLog {

  override val typ: LogType = LogType.JLog

  override def getTemplate(c: whitebox.Context)(t: String, isClass: Boolean): c.Tree = {
    import c.universe._
    if (isClass) {
      q"""private final val log: java.util.logging.Logger = java.util.logging.Logger.getLogger(classOf[${TypeName(t)}].getName)"""
    } else {
      q"""private final val log: java.util.logging.Logger = java.util.logging.Logger.getLogger(${TermName(t)}.getClass.getName)"""
    }
  }

}
