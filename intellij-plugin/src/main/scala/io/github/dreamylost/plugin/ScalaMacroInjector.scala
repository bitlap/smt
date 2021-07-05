package io.github.dreamylost.plugin

import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ ScClass, ScObject, ScTypeDefinition }
import org.jetbrains.plugins.scala.lang.psi.impl.toplevel.typedef.SyntheticMembersInjector

/**
 * Desc:
 *
 * Mail: chk19940609@gmail.com
 * Created by IceMimosa
 * Date: 2021/7/4
 */
class ScalaMacroInjector extends SyntheticMembersInjector {

  override def needsCompanionObject(source: ScTypeDefinition): Boolean = {
    source.hasAnnotation(ScalaMacroNames.BUILDER)
  }

  override def injectFunctions(source: ScTypeDefinition): Seq[String] = {
    val companionClass = source match {
      case obj: ScObject => obj.fakeCompanionClassOrCompanionClass
      case _             => null
    }

    companionClass match {
      case clazz: ScClass if clazz.hasAnnotation(ScalaMacroNames.BUILDER) =>
        Seq(s"""def builder(): ${clazz.getName} = ???""")
      case _ => Nil
    }
  }
}

object ScalaMacroInjector {

}
