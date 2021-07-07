package io.github.dreamylost

import io.github.dreamylost.logs._
import io.github.dreamylost.macros.logMacro

import scala.annotation.{ StaticAnnotation, compileTimeOnly }

/**
 * annotation to generate log.
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
