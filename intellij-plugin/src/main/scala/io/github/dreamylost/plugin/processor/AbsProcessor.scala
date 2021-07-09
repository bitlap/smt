package io.github.dreamylost.plugin.processor

import org.jetbrains.plugins.scala.lang.psi.api.base.ScMethodLike
import org.jetbrains.plugins.scala.lang.psi.api.statements.params.ScClassParameter
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScClass

/**
 * Mail: chk19940609@gmail.com
 * Created by IceMimosa
 * Date: 2021/6/30
 */
abstract class AbsProcessor extends Processor {

  override def needCompanion: Boolean = false

  /**
   * get constructor parameters
   *
   * @return name and type
   */
  protected def getConstructorParameters(clazz: ScClass, withSecond: Boolean = true): Seq[(String, String)] = {
    val constructors = if (withSecond) {
      clazz.constructors.map(Some(_))
    } else {
      Seq(clazz.constructor.map(_.asInstanceOf[ScMethodLike]))
    }
    constructors.filter(_.isDefined).map(_.get).flatMap(_.getParameterList.getParameters)
      .map {
        case p: ScClassParameter =>
          p.name -> p.`type`().toOption.map(_.toString).getOrElse("Unit")
        case _ => "" -> ""
      }
      .filter(_._1 != "")
  }
}
