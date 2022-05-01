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

import org.bitlap.cacheable.core.{ Cache, ZIOCache }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import zio.ZIO

import scala.util.Random

/**
 * @author 梦境迷离
 * @since 2022/3/20
 * @version 1.0
 */
class CustomCacheableTest extends AnyFlatSpec with Matchers {

  "create a custom cacheable by implicit" should "" in {
    implicit def cache: ZIOCache[Any, Throwable, String] = new ZIOCache[Any, Throwable, String] {
      override def getIfPresent(
        business: => ZIO[Any, Throwable, String]
      )(identities: List[String], args: List[_]): ZIO[Any, Throwable, String] = {
        println("hello world!!")
        business
      }
    }

    def readIOFunction(id: Int, key: String): ZIO[Any, Throwable, String] = {
      val $result = ZIO.effect("hello world" + Random.nextInt())
      Cache($result)(List("UseCaseExample", "readIOFunction"), List(id, key))
    }

    val ret = zio.Runtime.default.unsafeRun(readIOFunction(1, ""))
    ret.startsWith("hello world") shouldBe true
  }
}
