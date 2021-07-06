package io.github.dreamylost

import io.github.dreamylost.macros.synchronizedMacro

import scala.annotation.{ StaticAnnotation, compileTimeOnly }

/**
 * annotation to generate synchronized for methods.
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
