package io.github.dreamylost.logs

import io.github.dreamylost.PACKAGE

object LogType extends Enumeration {

  type LogType = Value
  val JLog, Log4j2, Slf4j = Value

  private lazy val types = Map(
    JLog -> JLogImpl,
    Log4j2 -> Log4J2Impl,
    Slf4j -> Slf4jImpl
  )

  def getLogImpl(logType: LogType): BaseLog = {
    types.getOrElse(logType, default = throw new Exception(s"Not support log type: $logType"))
  }

  def getLogType(shortType: String): LogType = {
    val tpe = PACKAGE + "." + shortType
    val v = LogType.values.find(p => {
      s"$PACKAGE.${p.toString}" == tpe || s"$PACKAGE.$LogType.${p.toString}" == tpe
    }).getOrElse(throw new Exception(s"Not support log type: $shortType")).toString
    LogType.withName(v)
  }
}

