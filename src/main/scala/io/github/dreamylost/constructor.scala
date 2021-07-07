package io.github.dreamylost

import io.github.dreamylost.macros.constructorMacro

import scala.annotation.{ StaticAnnotation, compileTimeOnly }

/**
 * annotation to generate secondary constructor method for classes.
 *
 * @author 梦境迷离
 * @param verbose       Whether to enable detailed log.
 * @param excludeFields Whether to exclude the specified var fields.
 * @since 2021/7/3
 * @version 1.0
 */
@compileTimeOnly("enable macro to expand macro annotations")
final class constructor(
    verbose:       Boolean     = false,
    excludeFields: Seq[String] = Nil
) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro constructorMacro.impl
}
