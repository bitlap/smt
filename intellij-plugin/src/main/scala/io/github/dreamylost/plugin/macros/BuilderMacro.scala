package io.github.dreamylost.plugin.macros

import io.github.dreamylost.plugin.utils.Utils
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScClass

/**
 * builder macro support
 *
 * @author 梦境迷离
 * @since 2021/7/5
 * @version 1.0
 */
object BuilderMacro extends ScalaMacroTemplate {

  private final val BuilderClassName = "Builder"

  //TODO better way
  override def classMacroTemplate(clazz: ScClass): String = {
    val className = clazz.getName()
    // TODO the first constructor
    val nameAndTypes = clazz.getConstructors.head.getParameterList.getParameters.map(p => p.getName -> Option(p.getType.getCanonicalText()).getOrElse("Unit"))
    val builderClassDef =
      s"""
         | class $BuilderClassName {
         |${
        nameAndTypes.map(term => s"private var ${term._1}: ${Utils.convert2ScalaType(term._2)} = _;" +
          s" def ${term._1}(${term._1}: ${Utils.convert2ScalaType(term._2)}): Builder = ???").mkString("\n")
      }
         |   def build(): $className = ???
         |  }
         |""".stripMargin
    println(s"classMacroTemplate: builderClassDef ===> $builderClassDef")
    builderClassDef
  }

  override def methodMacroTemplate(clazz: ScClass): String = {
    val builderMethod = s"""def builder(): $BuilderClassName = ???"""
    println(s"methodMacroTemplate: builderMethod ===> $builderMethod")
    builderMethod
  }
}
