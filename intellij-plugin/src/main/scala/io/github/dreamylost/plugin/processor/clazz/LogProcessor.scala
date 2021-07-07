package io.github.dreamylost.plugin.processor.clazz

import io.github.dreamylost.plugin.ScalaMacroNames
import io.github.dreamylost.plugin.processor.{ AbsProcessor, ProcessType }
import io.github.dreamylost.plugin.processor.ProcessType.ProcessType
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ ScClass, ScObject, ScTypeDefinition }

/**
 * Desc: Processor for annotation log
 *
 * Mail: chk19940609@gmail.com
 * Created by IceMimosa
 * Date: 2021/7/6
 */
class LogProcessor extends AbsProcessor {

  // default log expr
  private def logExpr(log: String = "java.util.logging.Logger") = s"private final val log: $log = ???"

  override def process(source: ScTypeDefinition, typ: ProcessType): Seq[String] = {
    typ match {
      case ProcessType.Field =>
        source match {
          case clazz @ (_: ScClass | _: ScObject) =>
            val an = clazz.annotations(ScalaMacroNames.LOG).last
            // annotation expr string
            an.annotationExpr.getText match {
              case expr if expr.contains("Slf4j") =>
                Seq(logExpr("org.slf4j.Logger"))
              case expr if expr.contains("Log4j2") =>
                Seq(logExpr("org.apache.logging.log4j.Logger"))
              case _ =>
                Seq(logExpr())
            }
          case _ => Nil
        }
      case _ => Nil
    }
  }
}
