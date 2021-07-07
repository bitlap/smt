package io.github.dreamylost.logs

import io.github.dreamylost.logs.LogType.LogType

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

object Log4J2Impl extends BaseLog {

  override val typ: LogType = LogType.Log4j2

  override def getTemplate(c: whitebox.Context)(t: String, isClass: Boolean): c.Tree = {
    import c.universe._
    if (isClass) {
      q"""private final val log: org.apache.logging.log4j.Logger = org.apache.logging.log4j.LogManager.getLogger(classOf[${TypeName(t)}].getName)"""
    } else {
      q"""private final val log: org.apache.logging.log4j.Logger = org.apache.logging.log4j.LogManager.getLogger(${TermName(t)}.getClass.getName)"""
    }
  }

}
