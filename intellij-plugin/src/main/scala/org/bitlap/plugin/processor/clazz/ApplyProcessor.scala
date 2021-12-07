package org.bitlap.plugin.processor.clazz

import org.bitlap.plugin.processor.ProcessType.ProcessType
import org.bitlap.plugin.processor.{ AbsProcessor, ProcessType }
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ ScClass, ScObject, ScTypeDefinition }

/**
 * Desc: Processor for annotation apply
 *
 * Mail: chk19940609@gmail.com
 * Created by IceMimosa
 * Date: 2021/7/8
 */
class ApplyProcessor extends AbsProcessor {

  override def needCompanion: Boolean = true

  override def process(source: ScTypeDefinition, typ: ProcessType): Seq[String] = {
    typ match {
      case ProcessType.Method =>
        source match {
          case obj: ScObject =>
            val clazz = obj.fakeCompanionClassOrCompanionClass.asInstanceOf[ScClass]
            val nameAndTypes = getConstructorCurryingParameters(clazz, withSecond = false)
              .map(_.map(o => s"${o._1}: ${o._2}").mkString("(", ", ", ")"))
              .mkString
            val genericType = getTypeParamString(clazz)
            val returnGenericType = getTypeParamString(clazz, returnType = true)
            Seq(s"def apply$genericType$nameAndTypes: ${clazz.getName}$returnGenericType = ???")
          case _ => Nil
        }
      case _ => Nil
    }
  }
}
