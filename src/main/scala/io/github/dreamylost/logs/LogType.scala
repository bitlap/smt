package io.github.dreamylost.logs

import scala.language.experimental.macros

object LogType extends Enumeration {

  type LogType = Value
  val JLog, Log4j2, Slf4j = Value

  private lazy val types = Map(
    JLog -> JLogImpl,
    Log4j2 -> Log4J2Impl,
    Slf4j -> Slf4jImpl
  )

  def getLogImpl(logType: LogType): BaseLog = {
    types.getOrElse(logType, default = throw new Exception(s"Not support log: $logType"))
  }
}

