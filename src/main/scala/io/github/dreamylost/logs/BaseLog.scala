package io.github.dreamylost.logs

import io.github.dreamylost.logs.LogType.LogType

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

trait BaseLog {
  val typ: LogType

  def getTemplate(c: whitebox.Context)(t: String, isClass: Boolean): c.Tree
}
