package io.github.dreamylost

/**
 *
 * @author 梦境迷离
 * @since 2021/6/29
 * @version 1.0
 */
object LogMain extends App {

  private final val log: java.util.logging.Logger = java.util.logging.Logger.getLogger(LogMain.getClass.getName)

  // object is not type
  private final val log2: org.apache.logging.log4j.Logger = org.apache.logging.log4j.LogManager.getLogger(LogMain.getClass.getName)

  private final val log3: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(LogMain.getClass)

  log.info("hello1")
  log2.info("hello2")
  log3.info("hello3")

}
