package io.github.dreamylost

/**
 *
 * @author 梦境迷离
 * @since 2021/6/29
 * @version 1.0
 */
object LogMain extends App {

  private val log: java.util.logging.Logger = java.util.logging.Logger.getLogger(LogMain.getClass.getName)

  println(LogMain.getClass.getName)
  println(log)
}
