/*
 * Copyright (c) 2021 org.bitlap
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.bitlap

import org.bitlap.logs.LogType
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 *
 * @author 梦境迷离
 * @since 2021/6/28
 * @version 1.0
 */
class LogTest extends AnyFlatSpec with Matchers {

  "log1" should "ok on class" in {
    """@log(verbose=true) class TestClass1(val i: Int = 0, var j: Int) {
                log.info("hello")
             }""" should compile

    """@log class TestClass2(val i: Int = 0, var j: Int)""" should compile
    """@log() class TestClass3(val i: Int = 0, var j: Int)""" should compile
    """@log(verbose=true) class TestClass4(val i: Int = 0, var j: Int)""" should compile
    """@log(logType=org.bitlap.logs.LogType.JLog) class TestClass5(val i: Int = 0, var j: Int)""" should compile
    """@log(verbose=true, logType=org.bitlap.logs.LogType.JLog) class TestClass6(val i: Int = 0, var j: Int)""" should compile
  }

  "log3" should "ok on object" in {
    """@log(verbose=true) object TestClass1 {
              log.info("hello")
           }""" should compile

    """@log object TestClass2""" should compile
    """@log() object TestClass3""" should compile
    """@log(verbose=true) object TestClass4""" should compile
    """@log(logType=org.bitlap.logs.LogType.JLog) object TestClass5""" should compile
    """@log(verbose=true, logType=org.bitlap.logs.LogType.JLog) object TestClass6""" should compile
  }

  "log4 log4j2" should "ok on object" in {
    """@log(verbose=true) object TestClass1 {
              log.info("hello")
           }""" should compile

    """@log object TestClass2""" should compile
    """@log() object TestClass3""" should compile
    """@log(verbose=true) object TestClass4""" should compile
    """@log(logType=org.bitlap.logs.LogType.Log4j2) object TestClass5""" should compile
    """@log(verbose=true, logType=org.bitlap.logs.LogType.Log4j2) object TestClass6""" should compile
  }

  "log5 slf4j" should "ok on object" in {
    """@log(verbose=true) object TestClass1 {
              log.info("hello")
           }""" should compile

    """@log object TestClass2""" should compile
    """@log() object TestClass3""" should compile
    """@log(verbose=true) object TestClass4""" should compile
    """@log(logType=org.bitlap.logs.LogType.Slf4j) object TestClass5""" should compile
    """@log(verbose=true, logType=org.bitlap.logs.LogType.Slf4j) object TestClass6""" should compile
  }

  "log6 log4j2" should "ok on class" in {
    """@log(verbose=true) class TestClass1(val i: Int = 0, var j: Int) {
                log.info("hello")
             }""" should compile

    """@log class TestClass2(val i: Int = 0, var j: Int)""" should compile
    """@log() class TestClass3(val i: Int = 0, var j: Int)""" should compile
    """@log(verbose=true) class TestClass4(val i: Int = 0, var j: Int)""" should compile
    """@log(logType=org.bitlap.logs.LogType.Log4j2) class TestClass5(val i: Int = 0, var j: Int)""" should compile
    """@log(verbose=true, logType=org.bitlap.logs.LogType.Log4j2) class TestClass6(val i: Int = 0, var j: Int)""" should compile
  }

  "log7 slf4j" should "ok on class" in {
    """@log(verbose=true) class TestClass1(val i: Int = 0, var j: Int) {
                log.info("hello")
             }""" should compile

    """@toString @builder @log class TestClass2(val i: Int = 0, var j: Int)""" should compile //Use with multiple annotations
    """@log() class TestClass3(val i: Int = 0, var j: Int)""" should compile
    """@log(verbose=true) class TestClass4(val i: Int = 0, var j: Int)""" should compile
    """@log(logType=org.bitlap.logs.LogType.Slf4j) class TestClass5(val i: Int = 0, var j: Int)""" should compile
    """@log(verbose=true, logType=org.bitlap.logs.LogType.Slf4j) class TestClass6(val i: Int = 0, var j: Int)""" should compile
    """@log(verbose=true, logType=org.bitlap.logs.LogType.Slf4j) class TestClass6(val i: Int = 0, var j: Int){ log.info("hello world") }""" should compile
    """@log(logType = org.bitlap.logs.LogType.Slf4j) class TestClass6(val i: Int = 0, var j: Int){ log.info("hello world") }""" should compile //default verbose is false
  }

  "log8 slf4j" should "ok on class and has object" in {
    """@log(verbose=true) class TestClass1(val i: Int = 0, var j: Int) {
                    log.info("hello")
                 }""" should compile

    """@toString @builder @log class TestClass2(val i: Int = 0, var j: Int)""" should compile //Use with multiple annotations
    """@log() class TestClass3(val i: Int = 0, var j: Int)""" should compile
    """@log(verbose=true) class TestClass4(val i: Int = 0, var j: Int)""" should compile
    """@log(logType=org.bitlap.logs.LogType.Slf4j) class TestClass5(val i: Int = 0, var j: Int)""" should compile
    """@log(verbose=true, logType=org.bitlap.logs.LogType.Slf4j) class TestClass6(val i: Int = 0, var j: Int)""" should compile
    """@log(verbose=true, logType=org.bitlap.logs.LogType.Slf4j) class TestClass6(val i: Int = 0, var j: Int){ log.info("hello world") }""" should compile
    """@log(logType = org.bitlap.logs.LogType.Slf4j) @builder class TestClass6(val i: Int = 0, var j: Int){ log.info("hello world") }
          | @log(logType = org.bitlap.logs.LogType.Slf4j) object TestClass6 { log.info("hello world");builder() }""".stripMargin should compile //default verbose is false

    @log(logType = org.bitlap.logs.LogType.Slf4j)
    @builder class TestClass8(val i: Int = 0, var j: Int) {
      log.info("hello world")
    }
    object TestClass8 { builder() }
  }

  "log9 slf4j" should "ok on class and it object" in {
    """
      |@log(logType = org.bitlap.logs.LogType.Slf4j) @builder  class TestClass6(val i: Int = 0, var j: Int){ log.info("hello world") }
      |@log(logType = org.bitlap.logs.LogType.Slf4j) object TestClass6 { log.info("hello world"); builder()}
      |""".stripMargin should compile
  }

  "log10 slf4j" should "failed on case class" in {
    """
      |    @log(verbose=false, logType = LogType.JLog)
      |    @builder case class TestClass6_2(val i: Int = 0, var j: Int) {
      |      log.info("hello world")
      |    }
      |    @log(logType = org.bitlap.logs.LogType.Slf4j) object TestClass6_2 {
      |      log.info("hello world"); builder()
      |    }
      |""".stripMargin shouldNot compile
  }

  "log11 slf4j" should "ok on class and it object" in {
    """
      | @log(logType = org.bitlap.logs.LogType.Slf4j)
      | @builder class TestClass6(val i: Int = 0, var j: Int) {
      |      log.info("hello world")
      |    }
      |@log(logType = org.bitlap.logs.LogType.Slf4j) object TestClass6 {
      |      log.info("hello world"); builder()
      |    }
      |""".stripMargin should compile

    """
      | @builder
      | @log(logType = org.bitlap.logs.LogType.Slf4j)
      |    class TestClass6(val i: Int = 0, var j: Int) {
      |      log.info("hello world")
      |    }
      |@log(logType = org.bitlap.logs.LogType.Slf4j) object TestClass6 {
      |      log.info("hello world"); builder()
      |    }
      |""".stripMargin should compile
  }

  "log for plugin test" should "compile ok" in {
    @log class TestLog1() {
      log.info("")
    }
    @log object TestLog1 {
      log.info("")
    }
    @log(logType = org.bitlap.logs.LogType.Slf4j) class TestLog2() {
      log.info("")
    }
    import org.bitlap.logs.LogType.JLog
    @log(logType = JLog) class TestLog3() {
      log.info("")
    }
    @log(logType = LogType.Slf4j) class TestLog4() {
      log.info("")
    }
  }
  "log12 slf4j" should "failed when input not in order" in {
    """
      | import org.bitlap.logs.LogType
      | @log(logType = LogType.Slf4j, verbose = true)
      | @builder class TestClass6(val i: Int = 0, var j: Int) {
      |      log.info("hello world")
      |    }
      |""".stripMargin shouldNot compile
  }

  "log13 scala loggging lazy" should "ok when does not exists super class" in {
    """
      | import org.bitlap.logs.LogType
      | @log(logType = LogType.ScalaLoggingLazy)
      | class TestClass1(val i: Int = 0, var j: Int) {
      |      log.info("hello world")
      |    }
      |""".stripMargin should compile

    import org.bitlap.logs.LogType
    @log(logType = LogType.ScalaLoggingLazy)
    class TestClass2(val i: Int = 0, var j: Int) {
      log.info("hello world")
    }
    """
      | import org.bitlap.logs.LogType
      | @log(logType = LogType.ScalaLoggingLazy)
      | class TestClass3(val i: Int = 0, var j: Int) {
      |      log.info("hello world")
      |    }
      |""".stripMargin should compile

    """
      | import org.bitlap.logs.LogType
      | @log(logType = LogType.ScalaLoggingLazy)
      | object TestClass4 {
      |      log.info("hello world")
      |    }
      |""".stripMargin should compile
  }

  "log14 scala loggging strict" should "ok when exists super class" in {
    """
      | import org.bitlap.logs.LogType
      | @log(logType = LogType.ScalaLoggingStrict)
      | class TestClass1(val i: Int = 0, var j: Int) extends Serializable {
      |      log.info("hello world")
      |    }
      |""".stripMargin should compile

    import org.bitlap.logs.LogType
    @log(logType = LogType.ScalaLoggingStrict)
    class TestClass2(val i: Int = 0, var j: Int) extends Serializable {
      log.info("hello world")
    }
    """
      | import org.bitlap.logs.LogType
      | @log(logType = LogType.ScalaLoggingStrict)
      | class TestClass3(val i: Int = 0, var j: Int) extends Serializable {
      |      log.info("hello world")
      |    }
      |""".stripMargin should compile

    """
      | import org.bitlap.logs.LogType
      | @log(logType = LogType.ScalaLoggingStrict)
      | object TestClass4 extends Serializable {
      |      log.info("hello world")
      |    }
      |""".stripMargin should compile
  }

  "log15 add @transient" should "ok" in {
    """
      |val str = Json.toJson(TestClass1(1, 1, "hello")).toString()
      |val obj = Json.fromJson[TestClass1](str)
      |obj.log
      |""".stripMargin shouldNot compile
  }
}
