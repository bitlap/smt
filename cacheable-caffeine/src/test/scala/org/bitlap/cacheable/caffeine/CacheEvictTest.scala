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

package org.bitlap.cacheable.caffeine

import org.bitlap.cacheable.core.{ cacheEvict, cacheable }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import zio.ZIO
import zio.stream.ZStream

import scala.util.Random

/**
 *
 * @author 梦境迷离
 * @since 2022/3/19
 * @version 1.0
 */
class CacheEvictTest extends AnyFlatSpec with Matchers {

  val runtime = zio.Runtime.default

  // write readStreamFunction1 method , otherwise: The specified method: `readStreamFunction1` does not exist in enclosing class: `CacheEvictTest`!
  def readStreamFunction1: String = "hello world"

  def readStreamFunction: String = "hello world"

  def readIOFunction: String = "hello world"

  val readIOMethodName = "readIOFunction"
  val readStreamMethodName = "readStreamFunction"

  "cacheEvict1" should "expected annotation pattern" in {
    @cacheEvict(true, List())
    def updateFunction3(id: Int, key: String): ZIO[Any, Throwable, String] = {
      ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}")
    }

    @cacheEvict(local = true)
    def updateFunction4(id: Int, key: String): ZIO[Any, Throwable, String] = {
      ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}")
    }

    @cacheEvict(values = List())
    def updateFunction5(id: Int, key: String): ZIO[Any, Throwable, String] = {
      ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}")
    }

    @cacheEvict()
    def updateFunction6(id: Int, key: String): ZIO[Any, Throwable, String] = {
      ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}")
    }
  }

  "cacheEvict2" should "unexpected annotation pattern" in {
    """
      |    @cacheEvict(values = List("readStreamFunction1"), local = true)
      |    def updateFunction1(id: Int, key: String): ZIO[Any, Throwable, String] = {
      |      ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}")
      |    }
      |""".stripMargin shouldNot compile
  }

  "cacheEvict3" should "ok when return type is case class" in {
    @cacheEvict(local = true)
    def updateEntityFunction(id: Int, key: String): ZIO[Any, Throwable, CacheValue] = {
      ZIO.effect(CacheValue(Random.nextInt() + ""))
    }
  }

  "cacheEvict4" should "zio operation is ok with redis" in {
    val cacheValue = Random.nextInt().toString

    @cacheable(local = true)
    def readIOFunction(id: Int, key: String): ZIO[Any, Throwable, String] = {
      ZIO.effect(cacheValue)
    }

    @cacheEvict(local = true, values = List("readIOFunction"))
    def updateIOFunction(id: Int, key: String): ZIO[Any, Throwable, String] = {
      ZIO.effect(Random.nextInt() + "")
    }

    val result = runtime.unsafeRun(for {
      _ <- ZCaffeine.del("CacheableTest-" + readIOMethodName)
      read <- readIOFunction(1, "hello")
      update <- updateIOFunction(1, "hello")
      cache <- ZCaffeine.hGet[String]("CacheableTest-" + readIOMethodName, "1-hello")
    } yield cache)
    result shouldEqual None
  }

  "cacheEvict5" should "zstream operation is ok with redis" in {
    val cacheValue = Random.nextInt().toString

    @cacheable(local = true)
    def readStreamFunction(id: Int, key: String): ZStream[Any, Throwable, String] = {
      ZStream.fromEffect(ZIO.effect(cacheValue))
    }

    @cacheEvict(local = true, values = List("readStreamFunction"))
    def updateStreamFunction(id: Int, key: String): ZStream[Any, Throwable, String] = {
      ZStream.fromEffect(ZIO.effect(Random.nextInt() + ""))
    }

    val result = runtime.unsafeRun(for {
      _ <- ZCaffeine.del("CacheableTest-" + readStreamMethodName)
      read <- readStreamFunction(1, "hello").runHead
      update <- updateStreamFunction(1, "hello").runHead
      cache <- ZCaffeine.hGet[String]("CacheableTest-" + readStreamMethodName, "1-hello")
    } yield cache
    )
    result shouldEqual None
  }
}
