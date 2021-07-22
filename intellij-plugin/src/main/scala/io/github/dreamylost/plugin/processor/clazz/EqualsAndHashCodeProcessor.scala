package io.github.dreamylost.plugin.processor.clazz

import io.github.dreamylost.plugin.processor.ProcessType.ProcessType
import io.github.dreamylost.plugin.processor.{ AbsProcessor, ProcessType }
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ ScClass, ScTypeDefinition }

/**
 * Desc: Processor for annotation equalsAndHashCode
 *
 * Mail: chk19940609@gmail.com
 * Created by IceMimosa
 * Date: 2021/7/22
 */
class EqualsAndHashCodeProcessor extends AbsProcessor {

  override def needCompanion: Boolean = true

  override def process(source: ScTypeDefinition, typ: ProcessType): Seq[String] = {
    typ match {
      case ProcessType.Method =>
        source match {
          case _: ScClass =>
            Seq(s"def canEqual(that: Any): Boolean = ???")
          case _ => Nil
        }
      case _ => Nil
    }
  }
}
