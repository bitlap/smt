package org.bitlap.plugin

import com.intellij.openapi.components.ServiceManager
import org.bitlap.plugin.processor.ProcessType
import org.bitlap.plugin.processor.ProcessType.ProcessType
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScTypeDefinition
import org.jetbrains.plugins.scala.lang.psi.impl.toplevel.typedef.SyntheticMembersInjector
/**
 * Desc: main injector to handle scala macro annotations.
 *
 * Mail: chk19940609@gmail.com
 * Created by IceMimosa
 * Date: 2021/7/4
 */
class ScalaMacroInjector extends SyntheticMembersInjector {

  private lazy val provider = ServiceManager.getService(classOf[ScalaMacroProcessorProvider])

  override def needsCompanionObject(source: ScTypeDefinition): Boolean = {
    provider.findProcessors(source).exists(_.needCompanion)
  }

  override def injectFunctions(source: ScTypeDefinition): Seq[String] = inject(source, ProcessType.Method)
  override def injectInners(source: ScTypeDefinition): Seq[String] = inject(source, ProcessType.Inner)
  override def injectMembers(source: ScTypeDefinition): Seq[String] = inject(source, ProcessType.Field)
  override def injectSupers(source: ScTypeDefinition): Seq[String] = inject(source, ProcessType.Super)

  private def inject(source: ScTypeDefinition, typ: ProcessType): Seq[String] = {
    provider
      .findProcessors(source)
      .flatMap(_.process(source, typ))
      .filter(s => s != null && s.trim.nonEmpty)
  }
}

object ScalaMacroInjector
