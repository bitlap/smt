package io.github.dreamylost.plugin.processor.clazz

import io.github.dreamylost.plugin.ScalaMacroNames
import io.github.dreamylost.plugin.processor.ProcessType.ProcessType
import io.github.dreamylost.plugin.processor.{AbsProcessor, ProcessType}
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScMethodCall
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScVariableDefinition
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ScClass, ScTypeDefinition}
import org.jetbrains.plugins.scala.lang.psi.types.ScLiteralType

/**
 * Desc: Processor for annotation constructor
 *
 * Mail: chk19940609@gmail.com
 * Created by IceMimosa
 * Date: 2021/7/8
 */
class ConstructorProcessor extends AbsProcessor {

  private val excludeFieldsName = "excludeFields"

  override def needCompanion: Boolean = true

  override def process(source: ScTypeDefinition, typ: ProcessType): Seq[String] = {
    typ match {
      case ProcessType.Method =>
        source match {
          case clazz: ScClass =>
            val consFields = getConstructorParameters(clazz, withSecond = false)
            val excludeFields = clazz.annotations(ScalaMacroNames.CONSTRUCTOR).lastOption match {
              case Some(an) =>
                an.getParameterList.getAttributes.findLast(_.getAttributeName == excludeFieldsName)
                  .map { expr =>
                    expr.getDetachedValue.asInstanceOf[ScMethodCall].argumentExpressions.map(_.`type`().toOption)
                      .filter(_.isDefined)
                      .map(_.get)
                      .map {
                        case str: ScLiteralType => str.value.value.toString
                        case _ => ""
                      }
                      .filter(_.nonEmpty)
                      .mkString(", ")
                  }.getOrElse("")
              case None => ""
            }
            val varFields = clazz.extendsBlock.members
              .filter {
                case _: ScVariableDefinition => true // var
                case _ => false // ScPatternDefinition, ScFunctionDefinition
              }
              .flatMap { v =>
                val vd = v.asInstanceOf[ScVariableDefinition]
                vd.declaredNames.map(n => (n, vd.`type`().toOption.map(_.toString).getOrElse("Unit")))
              }
              .filter(v => !excludeFields.contains(v._1))

            val consFieldsStr = consFields.map(_._1).mkString(", ")
            val allFieldsStr = (consFields ++ varFields).map(f => s"${f._1}: ${f._2}").mkString(", ")

            Seq(s"def this($allFieldsStr) = this($consFieldsStr)")
          case _ => Nil
        }
      case _ => Nil
    }
  }
}
