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

package org.bitlap.tools

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

/**
 *
 * @author 梦境迷离
 * @since 2021/8/7
 * @version 1.0
 */
class ElapsedTest extends AnyFlatSpec with Matchers {

  "elapsed1" should "failed, not calculate anything, the return type is not specified" in {
    //Duration and TimeUnit must Full class name
    """
      |    class A {
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = org.bitlap.tools.LogLevel.INFO)
      |      def i = ???
      |    }
      |
      |    class B {
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = org.bitlap.tools.LogLevel.WARN)
      |      def j = ???
      |    }
      |
      |    class C {
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = org.bitlap.tools.LogLevel.DEBUG)
      |      def j = ???
      |    }
      |
      |    class D {
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = org.bitlap.tools.LogLevel.INFO)
      |      def i:String = ???
      |   }
      |    val a = new A()
      |    val b = new B()
      |    val c = new C()
      |    val d = new D()
      |""".stripMargin shouldNot compile
  }

  "elapsed2" should "ok, get the returnType of the method " in {
    //Duration and TimeUnit must Full class name
    """
      |class A {
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.NANOSECONDS), logLevel = org.bitlap.tools.LogLevel.INFO)
      |      def helloWorld: String = {
      |        println("hello world")
      |        "hello"
      |      }
      |
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = org.bitlap.tools.LogLevel.INFO)
      |      def helloScala: String = {
      |        Thread.sleep(2000)
      |        println("hello world")
      |        "hello"
      |      }
      |}
      |    val a = new A
      |    a.helloWorld
      |    a.helloScala
      |""".stripMargin should compile
  }

  "elapsed3" should "ok" in {
    //Duration and TimeUnit must Full class name
    """
      |    class A {
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.NANOSECONDS), logLevel = org.bitlap.tools.LogLevel.INFO)
      |      def helloWorld: String = {
      |        println("") ; println(""); ""
      |      }
      |
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = org.bitlap.tools.LogLevel.INFO)
      |      def helloScala1: String = { println("") ; println(""); ""}
      |
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = org.bitlap.tools.LogLevel.INFO)
      |      def helloScala2: String = { println("") ; println("");  "" }
      |
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = org.bitlap.tools.LogLevel.INFO)
      |      def helloScala3: String = {
      |        val s = "hello"
      |        val x = "world"
      |        return s
      |      }
      |    }
      |    val a = new A()
      |""".stripMargin should compile
  }

  "elapsed4" should "ok, return early" in {
    //Duration and TimeUnit must Full class name
    """
      |    class A {
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = org.bitlap.tools.LogLevel.INFO)
      |      def helloScala1: String = {
      |        val s = "hello"
      |        if (s == "hello") {
      |          return "world"
      |        }
      |        val x = "world"
      |        return s
      |      }
      |    }
      |
      |    val a = new A
      |    a.helloScala1
      |
      |     class B {
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = org.bitlap.tools.LogLevel.INFO)
      |      def helloScala11: String = {
      |        val s = "hello"
      |        if (s == "hello") {
      |          return "world" + "wooo"
      |        }
      |        val x = "world"
      |        return s
      |      }
      |    }
      |
      |    val b = new B()
      |""".stripMargin should compile
  }

  "elapsed5" should "ok, return Future" in {
    class A {

      private final val log3: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(classOf[A])

      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = org.bitlap.tools.LogLevel.INFO)
      def helloScala1: Future[String] = {
        Thread.sleep(1000)
        Future.successful("hello world")
      }

      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = org.bitlap.tools.LogLevel.DEBUG)
      def helloScala2: Future[String] = {
        Thread.sleep(2000)
        Future {
          "hello world"
        }(scala.concurrent.ExecutionContext.Implicits.global)
      }

      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = org.bitlap.tools.LogLevel.WARN)
      def helloScala3: Future[String] = Future {
        "hello world"
      }(scala.concurrent.ExecutionContext.Implicits.global)
    }
  }

  "elapsed6" should "failed, not support when only has one expr" in {
    class B {

      import scala.concurrent.Await
      import scala.concurrent.duration.Duration

      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = org.bitlap.tools.LogLevel.WARN)
      def helloScala(t: String): Future[String] = {
        Future(t)(scala.concurrent.ExecutionContext.Implicits.global)
      }

      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = org.bitlap.tools.LogLevel.WARN)
      def helloScala11(t: String): Future[String] = Future(t)(scala.concurrent.ExecutionContext.Implicits.global)

      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = org.bitlap.tools.LogLevel.INFO)
      def helloScala2: String = {
        val s = Future("")(scala.concurrent.ExecutionContext.Implicits.global)
        Await.result(helloScala("world"), Duration.Inf)
      }
    }
  }

  "elapsed7" should "ok at object but has runTime Error" in { //Why?
    """
      |    object A {
      |      private final val log1: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(A.getClass)
      |
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = org.bitlap.tools.LogLevel.INFO)
      |      def helloScala1: Future[String] = {
      |        Thread.sleep(1000)
      |        Future.successful("hello world")
      |      }
      |
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = org.bitlap.tools.LogLevel.DEBUG)
      |      def helloScala2: Future[String] = {
      |        Thread.sleep(2000)
      |        Future {
      |          "hello world"
      |        }(scala.concurrent.ExecutionContext.Implicits.global)
      |      }
      |
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = org.bitlap.tools.LogLevel.WARN)
      |      def helloScala3: Future[String] = Future {
      |        "hello world"
      |      }(scala.concurrent.ExecutionContext.Implicits.global)
      |    }
      |""".stripMargin should compile
  }

  "elapsed8" should "ok at input args" in {
    @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = LogLevel.WARN)
    def helloScala1: String = {
      println("")
      println("")
      "hello"
    }
    import org.bitlap.tools.LogLevel.WARN
    @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = WARN)
    def helloScala2: String = {
      println("")
      println("")
      "hello"
    }

    @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = org.bitlap.tools.LogLevel.WARN)
    def helloScala3: String = {
      println("")
      println("")
      "hello"
    }
  }

  "elapsed9" should "failed at input args" in {
    """
      |@elapsed(logLevel = org.bitlap.tools.LogLevel.WARN, limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS))
      |    def helloScala1: String = {
      |      println("")
      |      println("")
      |      "hello"
      |    }
      |""".stripMargin shouldNot compile //args not in order
  }
  "elapsed10" should "multi-return" in {
    class A {

      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = org.bitlap.tools.LogLevel.INFO)
      def j: Int = {
        var i = 1
        if (i == 1) {
          val h = 0
          var l = 0
          if (j == 0) {
            val h = 0
            var l = 0
            return 1
          } else {
            val j = 0
            return 0
          }
        } else {
          i
        }
        i
      }

      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = org.bitlap.tools.LogLevel.INFO)
      def k: Unit = {
        var i = 1
        if (i == 1) {
          val i = 0
          val k = 0
          if (i == 0) {
            1
          } else {
            2
          }
        } else {
          val u = 0
          0
        }

        var k = 1
        if (k == 1) {
          val i = 0
          val k = 0
          if (i == 0) {
            return ()
          } else {
            return ()
          }
        } else {
          val u = 0
          return u
        }
        1
      }

      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = org.bitlap.tools.LogLevel.INFO)
      def l: Int = {
        val i = 0
        for (i <- Seq(1)) {
          if (i == 1) {
            return 1 //not support
          }
        }
        0
      }

      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = org.bitlap.tools.LogLevel.INFO)
      def m: Int = {
        var i = 1
        if (i == 1) {
        } else {
          val u = 0
          return 0
        }

        if (i == 1) {
          return 1
        } else {
          val u = 0
          return 0
        }

        1
      }

    }
  }

  "elapsed11" should "failed at abstract method" in {
    """
      |abstract class A {
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = org.bitlap.tools.LogLevel.WARN)
      |      def hello:String
      |    }
      |""".stripMargin shouldNot compile
  }

}
