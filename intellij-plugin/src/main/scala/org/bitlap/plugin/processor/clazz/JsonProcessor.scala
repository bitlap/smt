package org.bitlap.plugin.processor.clazz

import org.bitlap.plugin.processor.ProcessType.ProcessType
import org.bitlap.plugin.processor.{ AbsProcessor, ProcessType }
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ ScObject, ScTypeDefinition }

/**
 * Desc: Processor for annotation json
 *
 * Mail: chk19940609@gmail.com
 * Created by IceMimosa
 * Date: 2021/7/7
 */
class JsonProcessor extends AbsProcessor {

  override def needCompanion: Boolean = true

  override def process(source: ScTypeDefinition, typ: ProcessType): Seq[String] = {
    typ match {
      case ProcessType.Field =>
        source match {
          case obj: ScObject =>
            val clazz = obj.fakeCompanionClassOrCompanionClass
            Seq(s"implicit val jsonAnnotationFormat = play.api.libs.json.Json.format[${clazz.getName}]")
          case _ => Nil
        }
      case _ => Nil
    }
  }
}
