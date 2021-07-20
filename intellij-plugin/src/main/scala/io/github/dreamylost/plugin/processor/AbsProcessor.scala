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
    this.getConstructorCurryingParameters(clazz, withSecond, withCurrying = false).head
  }

  /**
   * get constructor parameters with currying
   *
   * @return
   *   if `withCurrying` = true, return (name: type, name: type)(name: type)...
   *   else return (name: type, name: type, name: type, ...)
   */
  protected def getConstructorCurryingParameters(clazz: ScClass, withSecond: Boolean = true, withCurrying: Boolean = true): Seq[Seq[(String, String)]] = {
    val constructors = if (withSecond) {
      clazz.constructors.map(Some(_))
    } else {
      Seq(clazz.constructor.map(_.asInstanceOf[ScMethodLike]))
    }
    if (withCurrying) {
      constructors.flatten.flatMap { c =>
        c.effectiveParameterClauses.map(_.effectiveParameters.collect {
          case p: ScClassParameter =>
            p.name -> p.`type`().toOption.map(_.toString).getOrElse("Unit")
        })
      }
    } else {
      Seq(
        constructors.flatten.flatMap(_.getParameterList.getParameters)
          .collect {
            case p: ScClassParameter =>
              p.name -> p.`type`().toOption.map(_.toString).getOrElse("Unit")
          }
      )
    }
  }
}
