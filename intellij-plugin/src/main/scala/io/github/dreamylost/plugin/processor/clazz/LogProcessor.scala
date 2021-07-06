package io.github.dreamylost.plugin.processor.clazz

import io.github.dreamylost.plugin.ScalaMacroNames
import io.github.dreamylost.plugin.processor.{ AbsProcessor, ProcessType }
import io.github.dreamylost.plugin.processor.ProcessType.ProcessType
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ ScClass, ScObject, ScTypeDefinition }

/**
 * Desc: Processor for annotation toString
 *
 * Mail: chk19940609@gmail.com
 * Created by IceMimosa
 * Date: 2021/7/6
 */
class LogProcessor extends AbsProcessor {

  // annotation: log type name
  private val logTypeName = "logType"

  // default log expr
  private def logExpr(log: String = "java.util.logging.Logger") = s"private final val log: $log = ???"

  override def process(source: ScTypeDefinition, typ: ProcessType): Seq[String] = {
    typ match {
      case ProcessType.Field =>
        source match {
          case clazz @ (_: ScClass | _: ScObject) =>
            val an = clazz.annotations(ScalaMacroNames.LOG).last
            an.annotationExpr.getAttributes.findLast(_.name == logTypeName) match {
              case Some(kv) if kv.getChildren.exists(_.getText.equalsIgnoreCase("Slf4j")) =>
                Seq(logExpr("org.slf4j.Logger"))
              case Some(kv) if kv.getChildren.exists(_.getText.equalsIgnoreCase("Log4j2")) =>
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
