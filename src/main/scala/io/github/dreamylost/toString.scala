package io.github.dreamylost

import io.github.dreamylost.macros.toStringMacro

import scala.annotation.{ StaticAnnotation, compileTimeOnly }

/**
 * annotation to generate toString for classes.
 *
 * @author 梦境迷离
 * @param verbose               Whether to enable detailed log.
 * @param includeInternalFields Whether to include the fields defined within a class.
 * @param includeFieldNames     Whether to include the name of the field in the toString.
 * @param callSuper             Whether to include the super's toString.
 * @since 2021/6/13
 * @version 1.0
 */
@compileTimeOnly("enable macro to expand macro annotations")
final class toString(
    verbose:               Boolean = false,
    includeInternalFields: Boolean = true,
    includeFieldNames:     Boolean = true,
    callSuper:             Boolean = false
) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro toStringMacro.impl
}
