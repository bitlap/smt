package io.github.dreamylost

import org.scalatest.{ FlatSpec, Matchers }

/**
 *
 * @author 梦境迷离
 * @since 2021/6/28
 * @version 1.0
 */
class LogTest extends FlatSpec with Matchers {

  "log1" should "ok on class" in {
    """@log(verbose=true) class TestClass1(val i: Int = 0, var j: Int) {
              log.info("hello")
           }""" should compile

    """@log class TestClass2(val i: Int = 0, var j: Int)""" should compile
    """@log() class TestClass3(val i: Int = 0, var j: Int)""" should compile
    """@log(verbose=true) class TestClass4(val i: Int = 0, var j: Int)""" should compile
    """@log(logType=io.github.dreamylost.LogType.JLog) class TestClass5(val i: Int = 0, var j: Int)""" should compile
    """@log(verbose=true, logType=io.github.dreamylost.LogType.JLog) class TestClass6(val i: Int = 0, var j: Int)""" should compile
  }

  "log2" should "ok on case class" in {
    """@log(verbose=true) case class TestClass1(val i: Int = 0, var j: Int) {
              log.info("hello")
           }""" should compile

    """@log case class TestClass2(val i: Int = 0, var j: Int)""" should compile
    """@log() case class TestClass3(val i: Int = 0, var j: Int)""" should compile
    """@log(verbose=true) case class TestClass4(val i: Int = 0, var j: Int)""" should compile
    """@log(logType=io.github.dreamylost.LogType.JLog) case class TestClass5(val i: Int = 0, var j: Int)""" should compile
    """@log(verbose=true, logType=io.github.dreamylost.LogType.JLog) case class TestClass6(val i: Int = 0, var j: Int)""" should compile
  }

  "log3" should "ok on object" in {
    """@log(verbose=true) object TestClass1 {
            log.info("hello")
         }""" should compile

    """@log object TestClass2""" should compile
    """@log() object TestClass3""" should compile
    """@log(verbose=true) object TestClass4""" should compile
    """@log(logType=io.github.dreamylost.LogType.JLog) object TestClass5""" should compile
    """@log(verbose=true, logType=io.github.dreamylost.LogType.JLog) object TestClass6""" should compile
  }
}
