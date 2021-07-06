package io.github.dreamylost.plugin.processor.clazz
import io.github.dreamylost.plugin.processor.{ AbsProcessor, ProcessType }
import io.github.dreamylost.plugin.processor.ProcessType.ProcessType
import io.github.dreamylost.plugin.utils.Utils
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ ScClass, ScObject, ScTypeDefinition }
import org.jetbrains.plugins.scala.lang.psi.light.ScLightParameter

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
                Seq(s"""def builder(): ${clazz.getName()}$builderClassNameSuffix = ???""")
              case _ => Nil
            }
          case _ => Nil
        }

      // if class, inject builder class
      case ProcessType.Inner =>
        source match {
          case clazz: ScClass =>
            val className = clazz.getName()
            // TODO the first constructor
            val nameAndTypes = clazz.getConstructors.head
              .getParameterList.getParameters
              .map {
                case p: ScLightParameter =>
                  // TODO: fix java primitive names
                  p.getName -> Option(p.getType.getCanonicalText()).getOrElse("Unit")
              }
            val assignMethods = nameAndTypes.map(term =>
              s"def ${term._1}(${term._1}: ${Utils.convert2ScalaType(term._2)}): $className$builderClassNameSuffix = ???"
            )
            Seq(
              s"""
                 |class $className$builderClassNameSuffix {
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
}
