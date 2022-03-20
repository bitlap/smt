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

    val result = runtime.unsafeRun(for {
      _ <- ZRedisService.del("CacheableTest-readIOFunction")
      method <- readIOFunction(1, "hello")
      cache <- ZRedisService.hGet[String]("CacheableTest-readIOFunction", "1-hello")
    } yield method -> cache
    )
    Some(result._1) shouldEqual result._2
  }

  "cacheable9" should "zstream operation is ok with redis" in {
    val cacheValue = Random.nextInt().toString

    @cacheable
    def readStreamFunction(id: Int, key: String): ZStream[Any, Throwable, String] = {
      ZStream.fromEffect(ZIO.effect(cacheValue))
    }

    val result = runtime.unsafeRun(for {
      _ <- ZRedisService.del("CacheableTest-readStreamFunction")
      method <- readStreamFunction(1, "hello").runHead
      cache <- ZRedisService.hGet[String]("CacheableTest-readStreamFunction", "1-hello")
    } yield method -> cache
    )
    result._1 shouldEqual result._2
  }

  "cacheable10" should "entity zstream is ok with redis" in {
    val cacheValue = CacheValue(Random.nextInt().toString)

    @cacheable
    def readEntityStreamFunction(id: Int, key: String): ZStream[Any, Throwable, CacheValue] = {
      ZStream.fromEffect(ZIO.effect(cacheValue))
    }

    val result = runtime.unsafeRun(for {
      _ <- ZRedisService.del("CacheableTest-readEntityStreamFunction")
      method <- readEntityStreamFunction(1, "hello").runHead
    } yield method
    )

    result shouldEqual Some(cacheValue)
  }

  "cacheable11" should "entity zio is ok with redis" in {
    val cacheValue = CacheValue(Random.nextInt().toString)

    @cacheable
    def readEntityIOFunction(id: Int, key: String): ZIO[Any, Throwable, CacheValue] = {
      ZIO.effect(cacheValue)
    }

    val result = runtime.unsafeRun(for {
      _ <- ZRedisService.del("CacheableTest-readEntityIOFunction")
      method <- readEntityIOFunction(1, "hello")
      //      cache <- ZRedisService.hGet[CacheValue]("CacheableTest-readEntityIOFunction", "1-hello")
    } yield Some(method) -> Some(cacheValue)
    )

    result._1 shouldEqual result._2

  }
}
