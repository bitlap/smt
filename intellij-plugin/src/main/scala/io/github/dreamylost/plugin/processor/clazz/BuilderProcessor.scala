package io.github.dreamylost.plugin.processor.clazz

import io.github.dreamylost.plugin.processor.{ AbsProcessor, ProcessType }
import io.github.dreamylost.plugin.processor.ProcessType.ProcessType
import org.jetbrains.plugins.scala.lang.psi.api.statements.params.ScClassParameter
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ ScClass, ScObject, ScTypeDefinition }

/**
 * Desc: Processor for annotation builder
 *
 * Mail: chk19940609@gmail.com
 * Created by IceMimosa
 * Date: 2021/6/30
 */
class BuilderProcessor extends AbsProcessor {

  private final val builderClassNameSuffix = "Builder"

  override def needCompanion: Boolean = true

  override def process(source: ScTypeDefinition, typ: ProcessType): Seq[String] = {
    typ match {
      // if companion object, inject builder method
      case ProcessType.Method =>
        source match {
          case obj: ScObject =>
            obj.fakeCompanionClassOrCompanionClass match {
              case clazz: ScClass =>
                Seq(s"""def builder(): ${genBuilderName(clazz.getName, returnType = true)} = ???""")
              case _ => Nil
            }
          case _ => Nil
        }

      // if class, inject builder class
      case ProcessType.Inner =>
        source match {
          case obj: ScObject =>
            val clazz = obj.fakeCompanionClassOrCompanionClass
            val className = clazz.getName
            // support constructor and second constructor
            val nameAndTypes = getConstructorParameters(clazz.asInstanceOf[ScClass])
            val assignMethods = nameAndTypes.map(term =>
              s"def ${term._1}(${term._1}: ${term._2}): ${genBuilderName(className, returnType = true)} = ???"
            )
            Seq(
              s"""
                 |class ${genBuilderName(className)} {
                 |  def build(): $className = ???
                 |  ${assignMethods.mkString("\n")}
                 |}
                 |""".stripMargin
            )
          case _ => Nil
        }
      case _ => Nil
    }
  }

  /**
   * Gen class builder name
   *
   * @param className   base class name
   * @param returnType  if function or field return type
   */
  private def genBuilderName(className: String, returnType: Boolean = false): String = {
    if (returnType) {
      s"$className.$className$builderClassNameSuffix"
    } else {
      s"$className$builderClassNameSuffix"
    }
  }
}
