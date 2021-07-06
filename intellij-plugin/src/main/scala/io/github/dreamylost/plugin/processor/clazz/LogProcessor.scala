package io.github.dreamylost.plugin.processor.clazz

import io.github.dreamylost.plugin.processor.{ AbsProcessor, ProcessType }
import io.github.dreamylost.plugin.processor.ProcessType.ProcessType
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ ScClass, ScTypeDefinition }

/**
 * Desc: Processor for annotation toString
 *
 * Mail: chk19940609@gmail.com
 * Created by IceMimosa
 * Date: 2021/7/6
 */
class LogProcessor extends AbsProcessor {

  override def process(source: ScTypeDefinition, typ: ProcessType): Seq[String] = {
    typ match {
      case ProcessType.Field =>
        source match {
          case _: ScClass =>
            // TODO: support others log type
            Seq(s"private final val log: org.slf4j.Logger = ???")
          case _ => Nil
        }
      case _ => Nil
    }
  }
}
