/*
 * Copyright (c) 2021 jxnu-liguobin && contributors
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

package io.github.dreamylost

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

  "elapsed1" should "not calculate anything, the return type is not specified" in {
    //Duration and TimeUnit must Full class name
    """
      |    class A {
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = io.github.dreamylost.LogLevel.INFO)
      |      def i = ???
      |    }
      |
      |    class B {
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = io.github.dreamylost.LogLevel.WARN)
      |      def j = ???
      |    }
      |
      |    class C {
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = io.github.dreamylost.LogLevel.DEBUG)
      |      def j = ???
      |    }
      |
      |    class D {
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = io.github.dreamylost.LogLevel.ERROR)
      |      def j = ???
      |    }|
      |""".stripMargin shouldNot compile
  }

  "elapsed2" should "ok, get the returnType of the method " in {
    //Duration and TimeUnit must Full class name
    """
      |class A {
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.NANOSECONDS), logLevel = io.github.dreamylost.LogLevel.INFO)
      |      def helloWorld: String = {
      |        println("hello world")
      |        "hello"
      |      }
      |
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = io.github.dreamylost.LogLevel.INFO)
      |      def helloScala: String = {
      |        Thread.sleep(2000)
      |        println("hello world")
      |        "hello"
      |      }
      |    }
      |    val a = new A
      |    a.helloWorld
      |    a.helloScala
      |""".stripMargin should compile
  }

  "elapsed3" should "ok, when empty method or return" in {
    //Duration and TimeUnit must Full class name
    """
      |    class A {
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.NANOSECONDS), logLevel = io.github.dreamylost.LogLevel.INFO)
      |      def helloWorld: String = {
      |        println("") ; println(""); ""
      |      }
      |
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = io.github.dreamylost.LogLevel.INFO)
      |      def helloScala1: String = { println("") ; println(""); ""}
      |
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = io.github.dreamylost.LogLevel.INFO)
      |      def helloScala2: String = { println("") ; println("");  "" }
      |
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = io.github.dreamylost.LogLevel.INFO)
      |      def helloScala3: String = {
      |        val s = "hello"
      |        val x = "world"
      |        return s
      |      }
      |    }
      |""".stripMargin should compile
  }

  "elapsed4" should "ok, return early" in {
    //Duration and TimeUnit must Full class name
    """
      |    class A {
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = io.github.dreamylost.LogLevel.INFO)
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
      |""".stripMargin should compile

  }

  "elapsed5" should "ok, return Future" in {
    class A {

      private final val log3: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(classOf[A])

      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = io.github.dreamylost.LogLevel.INFO)
      def helloScala1: Future[String] = {
        Thread.sleep(1000)
        Future.successful("hello world")
      }

      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = io.github.dreamylost.LogLevel.DEBUG)
      def helloScala2: Future[String] = {
        Thread.sleep(2000)
        Future {
          "hello world"
        }(scala.concurrent.ExecutionContext.Implicits.global)
      }

      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = io.github.dreamylost.LogLevel.WARN)
      def helloScala3: Future[String] = Future {
        "hello world"
      }(scala.concurrent.ExecutionContext.Implicits.global)
    }
  }

  "elapsed6" should "failed, not support when only has one expr" in {
    """
      |    class B {
      |
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = io.github.dreamylost.LogLevel.WARN)
      |      def helloScala1(t: String): Future[String] = {
      |        Future(t)(scala.concurrent.ExecutionContext.Implicits.global)
      |      }
      |
      |      @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = io.github.dreamylost.LogLevel.INFO)
      |      def helloScala2: String = Await.result(helloScala1("world"), Duration.Inf)
      |
      |    }
      |""".stripMargin shouldNot compile
  }
}
