package io.github.dreamylost.plugin.processor.clazz

import io.github.dreamylost.plugin.ScalaMacroNames
import io.github.dreamylost.plugin.processor.ProcessType.ProcessType
import io.github.dreamylost.plugin.processor.{ AbsProcessor, ProcessType }
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScMethodCall
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScVariableDefinition
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ ScClass, ScTypeDefinition }
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
                // get excludeFields function call
                an.getParameterList.getAttributes.findLast(_.getAttributeName == excludeFieldsName).map(_.getDetachedValue)
                  .collect {
                    case call: ScMethodCall =>
                      // get call parameters
                      call.argumentExpressions.flatMap(_.`type`().toOption)
                        .collect {
                          case str: ScLiteralType => str.value.value.toString
                        }
                        .mkString(", ")
                  }.getOrElse("")
              case None => ""
            }
            val varFields = clazz.extendsBlock.members
              .collect {
                // var, others: ScPatternDefinition, ScFunctionDefinition
                case `var`: ScVariableDefinition => `var`
              }
              .flatMap { v =>
                v.declaredNames.map(n => (n, v.`type`().toOption.map(_.toString).getOrElse("Unit")))
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
