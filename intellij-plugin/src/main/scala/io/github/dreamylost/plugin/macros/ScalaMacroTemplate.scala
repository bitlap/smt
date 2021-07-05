package io.github.dreamylost.plugin.macros

import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScClass

/**
 *
 * @author 梦境迷离
 * @since 2021/7/5
 * @version 1.0
 */
trait ScalaMacroTemplate {

  /**
   * macro template for method.
   *
   * @param clazz
   * @return
   */
  def methodMacroTemplate(clazz: ScClass): String

  /**
   * macro template for class.
   *
   * @param clazz
   * @return
   */
  def classMacroTemplate(clazz: ScClass): String = ???

  /**
   * macro template for expr.
   *
   * @param clazz
   * @return
   */
  def exprMacroTemplate(clazz: ScClass): String = ???
}
