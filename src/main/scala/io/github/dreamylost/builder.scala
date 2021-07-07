package io.github.dreamylost

import io.github.dreamylost.macros.builderMacro

import scala.annotation.{ StaticAnnotation, compileTimeOnly }

/**
 * annotation to generate builder pattern for classes.
 *
 * @author 梦境迷离
 * @since 2021/6/19
 * @version 1.0
 */
@compileTimeOnly("enable macro to expand macro annotations")
final class builder extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro builderMacro.impl
}
