/*
 * Copyright (c) 2022 bitlap
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

package org.bitlap.cacheable

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import zio.stream.ZStream
import zio.{ Task, ZIO }

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.Random

/**
 *
 * @author 梦境迷离
 * @since 2021/8/7
 * @version 1.0
 */
class CacheableTest extends AnyFlatSpec with Matchers {

  val runtime = zio.Runtime.default
  val readIOMethodName = "readIOFunction"
  val readStreamMethodName = "readStreamFunction"

  "cacheable1" should "ok" in {
    @cacheable
    def readStreamFunction1(id: Int, key: String): ZStream[Any, Throwable, String] = {
      ZStream.fromEffect(ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}"))
    }

    @cacheable
    def readAliasStreamFunction2(id: Int, key: String): zio.stream.Stream[Throwable, String] = {
      ZStream.fromEffect(ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}"))
    }
  }

  "cacheable2" should "ok" in {
    @cacheable
    def readFunction(id: Int, key: String): ZIO[Any, Throwable, String] = {
      ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}")
    }
  }

  "cacheable3" should "String cannot compile" in {
    """
      |    @cacheable
      |    def readFunction(id: Int, key: String): String = {
      |      s"hello world--$id-$key-${Random.nextInt()}"
      |    }
      |""".stripMargin shouldNot compile
  }

  "cacheable4" should "need specified type, otherwise cannot compile" in {
    """
      |    @cacheable
      |    def readFunction(id: Int, key: String) = {
      |      s"hello world--$id-$key-${Random.nextInt()}"
      |    }
      |""".stripMargin shouldNot compile
  }

  "cacheable5" should "expected annotation pattern" in {
    @cacheable(verbose = true)
    def readFunction1(id: Int, key: String): Task[String] = {
      ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}")
    }

    @cacheable(true)
    def readFunction2(id: Int, key: String): Task[String] = {
      ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}")
    }
  }

  "cacheable6" should "ok when return type is case class" in {
    @cacheable
    def readEntityFunction(id: Int, key: String): ZIO[Any, Throwable, CacheValue] = {
      ZIO.effect(CacheValue(Random.nextInt() + ""))
    }
  }

  "cacheable8" should "zio operation is ok with redis" in {
    val cacheValue = Random.nextInt().toString

    @cacheable
    def readIOFunction(id: Int, key: String): ZIO[Any, Throwable, String] = {
      ZIO.effect(cacheValue)
    }

    println(cacheValue)

    val method = runtime.unsafeRunToFuture(readIOFunction(1, "hello"))
    val methodResult = Await.result(method.future, 10.seconds)
    println("methodResult:" + methodResult)

    val cache = runtime.unsafeRunToFuture(ZRedisService.hGet[String]("CacheableTest-" + readIOMethodName, "1-hello"))
    val cacheResult = Await.result(cache.future, 10.seconds)
    println("cacheResult:" + cacheResult)
    Some(methodResult) shouldEqual cacheResult
  }

  "cacheable9" should "zstream operation is ok with redis" in {
    val cacheValue = Random.nextInt().toString

    @cacheable
    def readStreamFunction(id: Int, key: String): ZStream[Any, Throwable, String] = {
      ZStream.fromEffect(ZIO.effect(cacheValue))
    }

    println(cacheValue)

    val method = runtime.unsafeRunToFuture(readStreamFunction(1, "hello").runHead)
    val methodResult = Await.result(method.future, 10.seconds)
    println("methodResult:" + methodResult)

    val cache = runtime.unsafeRunToFuture(ZRedisService.hGet[String]("CacheableTest-" + readStreamMethodName, "1-hello"))
    val cacheResult = Await.result(cache.future, 10.seconds)
    println("cacheResult:" + cacheResult)
    methodResult shouldEqual cacheResult
  }
}
