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

package org.bitlap.cacheable.redis

import org.bitlap.cacheable.core.cacheable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import zio.stream.ZStream
import zio.{ Task, ZIO }
import zio.Chunk

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
    @cacheable(local = false)
    def readStreamFunction1(id: Int, key: String): ZStream[Any, Throwable, String] = {
      ZStream.fromEffect(ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}"))
    }
  }

  "cacheable2" should "expected annotation pattern" in {
    @cacheable(false)
    def readFunction2(id: Int, key: String): Task[String] = {
      ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}")
    }
  }

  "cacheable3" should "ok when return type is case class" in {
    @cacheable(local = false)
    def readEntityFunction(id: Int, key: String): ZIO[Any, Throwable, CacheValue] = {
      ZIO.effect(CacheValue(Random.nextInt() + ""))
    }
  }

  "cacheable4" should "zstream operation is ok with redis" in {
    val chunk = Chunk(Random.nextInt().toString, Random.nextInt().toString, Random.nextInt().toString)

    @cacheable(local = false)
    def readStreamFunction(id: Int, key: String): ZStream[Any, Throwable, String] = {
      ZStream.fromIterable(chunk)
    }

    println(chunk)
    val result = runtime.unsafeRun(for {
      _ <- ZRedisService.del("CacheableTest-readStreamFunction")
      method <- readStreamFunction(1, "hello").runCollect
      cache <- ZRedisService.hGet[Chunk[String]]("CacheableTest-readStreamFunction", "1-hello")
    } yield method -> cache.getOrElse(Chunk.empty)
    )
    result._1 shouldEqual result._2
  }

  "cacheable5" should "entity zstream is ok with redis" in {
    val cacheValue = CacheValue(Random.nextInt().toString)

    @cacheable(local = false)
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

  "cacheable6" should "entity zio is ok with redis" in {
    val cacheValue = CacheValue(Random.nextInt().toString)

    @cacheable(local = false)
    def readEntityIOFunction(id: Int, key: String): ZIO[Any, Throwable, CacheValue] = {
      ZIO.effect(cacheValue)
    }

    val result = runtime.unsafeRun(for {
      _ <- ZRedisService.del("CacheableTest-readEntityIOFunction")
      method <- readEntityIOFunction(1, "hello")
      cache <- ZRedisService.hGet[CacheValue]("CacheableTest-readEntityIOFunction", "1-hello")
    } yield Some(method) -> cache
    )

    result._1 shouldEqual result._2

  }
}
