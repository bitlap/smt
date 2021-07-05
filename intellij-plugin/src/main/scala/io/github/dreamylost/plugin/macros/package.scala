package io.github.dreamylost.plugin

import io.github.dreamylost.plugin.macros.ScalaMacroTemplateType.ScalaMacroActionType
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

  implicit class ScClassProxy(clazz: ScClass) {
    def extraTemplate(scalaMacroType: ScalaMacroType, scalaMacroActionType: ScalaMacroActionType): String = {
      scalaMacroType match {
        case ScalaMacroType.APPLY => scalaMacroActionType match {
          //          case ScalaMacroActionType.CLASS => ApplyMacro.classMacroTemplate(clazz)
          //          case ScalaMacroActionType.EXPR => ApplyMacro.exprMacroTemplate(clazz)
          case ScalaMacroTemplateType.METHOD => ApplyMacro.methodMacroTemplate(clazz)
        }
        case ScalaMacroType.BUILDER => scalaMacroActionType match {
          case ScalaMacroTemplateType.CLASS  => BuilderMacro.classMacroTemplate(clazz)
          //          case ScalaMacroActionType.EXPR => BuilderMacro.exprMacroTemplate(clazz)
          case ScalaMacroTemplateType.METHOD => BuilderMacro.methodMacroTemplate(clazz)
        }
        case ScalaMacroType.LOG => scalaMacroActionType match {
          //          case ScalaMacroActionType.CLASS => LogMacro.classMacroTemplate(clazz)
          case ScalaMacroTemplateType.EXPR => LogMacro.exprMacroTemplate(clazz)
          //          case ScalaMacroActionType.METHOD => LogMacro.methodMacroTemplate(clazz)
        }
        case ScalaMacroType.CONSTRUCTOR => scalaMacroActionType match {
          //          case ScalaMacroActionType.CLASS => ConstructorMacro.classMacroTemplate(clazz)
          //          case ScalaMacroActionType.EXPR => ConstructorMacro.exprMacroTemplate(clazz)
          case ScalaMacroTemplateType.METHOD => ConstructorMacro.methodMacroTemplate(clazz)
        }
      }
    }
  }
}
