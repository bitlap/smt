package io.github.dreamylost

import io.github.dreamylost.macros.jsonMacro

import scala.annotation.{ StaticAnnotation, compileTimeOnly }

/**
 * annotation to generate play-json implicit object for case classes.
 *
 * @author 梦境迷离
 * @since 2021/6/13
 * @version 1.0
 */
@compileTimeOnly("enable macro to expand macro annotations")
final class json extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro jsonMacro.impl
}
