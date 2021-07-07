package io.github.dreamylost

import io.github.dreamylost.macros.applyMacro

import scala.annotation.{ StaticAnnotation, compileTimeOnly }

/**
 * annotation to generate apply method for primary construction of ordinary classes.
 *
 * @author 梦境迷离
 * @param verbose Whether to enable detailed log.
 * @since 2021/6/30
 * @version 1.0
 */
@compileTimeOnly("enable macro to expand macro annotations")
final class apply(
    verbose: Boolean = false
) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro applyMacro.impl
}
