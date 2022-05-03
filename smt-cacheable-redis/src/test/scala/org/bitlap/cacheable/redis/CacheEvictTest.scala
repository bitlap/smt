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

import org.bitlap.cacheable.core.cacheEvict
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import zio.ZIO
import zio.stream.ZStream

import scala.util.Random

/**
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

  "cacheEvict1" should "ok" in {
    @cacheEvict(local = false, values = List("readStreamFunction1"))
    def updateStreamFunction1(id: Int, key: String): ZStream[Any, Throwable, String] =
      ZStream.fromEffect(ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}"))

    @cacheEvict(local = false, values = List("readStreamFunction1"))
    def updateAliasStreamFunction2(id: Int, key: String): zio.stream.Stream[Throwable, String] =
      ZStream.fromEffect(ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}"))
  }

  "cacheEvict2" should "ok" in {
    @cacheEvict(local = false, values = List("readStreamFunction1"))
    def updateFunction(id: Int, key: String): ZIO[Any, Throwable, String] =
      ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}")
  }

  "cacheEvict3" should "String cannot compile" in {
    """
      |    @cacheEvict(local = false, values = List("readStreamFunction1"))
      |    def updateFunction(id: Int, key: String): String = {
      |      s"hello world--$id-$key-${Random.nextInt()}"
      |    }
      |""".stripMargin shouldNot compile
  }

  "cacheEvict4" should "need specified type, otherwise cannot compile" in {
    """
      |    @cacheEvict(local = false, values = List("readStreamFunction1"))
      |    def updateFunction(id: Int, key: String) = {
      |      s"hello world--$id-$key-${Random.nextInt()}"
      |    }
      |""".stripMargin shouldNot compile
  }

  "cacheEvict5" should "compile failed when function name not found" in {
    """
      |    @cacheEvict(local = false, values = List("1212"))
      |    def updateFunction(id: Int, key: String): ZIO[Any, Throwable, String] = {
      |      ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}")
      |    }
      |""".stripMargin shouldNot compile
  }

  "cacheEvict6" should "expected annotation pattern" in {
    @cacheEvict(local = false, values = List("readStreamFunction1"))
    def updateFunction1(id: Int, key: String): ZIO[Any, Throwable, String] =
      ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}")

    @cacheEvict(local = false, values = List("readStreamFunction1"))
    def updateFunction2(id: Int, key: String): ZIO[Any, Throwable, String] =
      ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}")
  }

  "cacheEvict7" should "unexpected annotation pattern" in {
    """
      |    @cacheEvict(values = List("readStreamFunction1"), local = true)
      |    def updateFunction1(id: Int, key: String): ZIO[Any, Throwable, String] = {
      |      ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}")
      |    }
      |    
      |    @cacheEvict(false, values = List("readStreamFunction1"))
      |    def updateFunction2(id: Int, key: String): ZIO[Any, Throwable, String] = {
      |      ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}")
      |    }
      |""".stripMargin shouldNot compile
  }

}
