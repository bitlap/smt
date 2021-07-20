package io.github.dreamylost

import io.github.dreamylost.macros.equalsAndHashCodeMacro

import scala.annotation.{ StaticAnnotation, compileTimeOnly }

/**
 * annotation to generate equals and hashcode method for classes.
 *
 * @author 梦境迷离
 * @param verbose       Whether to enable detailed log.
 * @param excludeFields Whether to exclude the specified internal fields.
 * @since 2021/7/18
 * @version 1.0
 */
@compileTimeOnly("enable macro to expand macro annotations")
final class equalsAndHashCode(
    verbose:       Boolean     = false,
    excludeFields: Seq[String] = Nil
) extends StaticAnnotation {

  def macroTransform(annottees: Any*): Any = macro equalsAndHashCodeMacro.impl
}
