package io.github.dreamylost.plugin

import io.github.dreamylost.plugin.macros.ScalaMacroType.ScalaMacroType
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScClass

/**
 *
 * @author 梦境迷离
 * @since 2021/7/5
 * @version 1.0
 */
package object macros {

  object ScalaMacroType extends Enumeration {
    type ScalaMacroType = Value
    val BUILDER: Value = Value("io.github.dreamylost.builder")
    val LOG: Value = Value("io.github.dreamylost.log")
    val APPLY: Value = Value("io.github.dreamylost.apply")
    val CONSTRUCTOR: Value = Value("io.github.dreamylost.constructor")
    // TODO
  }

  sealed trait ScalaMacroTemplate {
    def macroTemplate(clazz: ScClass): String
  }

  implicit class ScClassProxy(clazz: ScClass) {
    def extraTemplate(scalaMacroType: ScalaMacroType): String = {
      scalaMacroType match {
        case ScalaMacroType.APPLY       => ApplyMacro.macroTemplate(clazz)
        case ScalaMacroType.BUILDER     => BuilderMacro.macroTemplate(clazz)
        case ScalaMacroType.LOG         => LogMacro.macroTemplate(clazz)
        case ScalaMacroType.CONSTRUCTOR => ConstructorMacro.macroTemplate(clazz)
      }
    }
  }

  object ApplyMacro extends ScalaMacroTemplate {
    override def macroTemplate(clazz: ScClass): String = ???
  }

  object BuilderMacro extends ScalaMacroTemplate {
    override def macroTemplate(clazz: ScClass): String = {
      s"""def builder(): ${clazz.getName} = ???"""
    }
  }

  object LogMacro extends ScalaMacroTemplate {
    override def macroTemplate(clazz: ScClass): String = ???
  }

  object ConstructorMacro extends ScalaMacroTemplate {
    override def macroTemplate(clazz: ScClass): String = ???
  }

}
