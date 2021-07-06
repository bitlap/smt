package io.github.dreamylost.logs

import io.github.dreamylost.logs.LogType.LogType

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

object Slf4jImpl extends BaseLog {

  override val typ: LogType = LogType.Slf4j

  override def getTemplate(c: whitebox.Context)(t: String, isClass: Boolean): c.Tree = {
    import c.universe._
    if (isClass) {
      q"""private final val log: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(classOf[${TypeName(t)}])"""
    } else {
      q"""private final val log: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(${TermName(t)}.getClass)"""
    }
  }
}
