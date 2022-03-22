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

package org.bitlap.tools

import scala.util.Random
import org.bitlap.cacheable.core.{ Cache, LogUtils }
import org.bitlap.cacheable.caffeine.Implicits._
import zio.console.putStrLn
import zio.stream.ZStream
import zio.{ ExitCode, UIO, URIO, ZIO }
import org.bitlap.cacheable.core.cacheable

/**
 * use these function to test it.
 *
 * @author 梦境迷离
 * @version 1.0,2022/3/18
 */
object CacheExample extends zio.App {

  // import org.bitlap.cacheable.caffeine.Implicits._
  def readAliasStreamFunction(id: Int, key: String): zio.stream.Stream[Throwable, String] = {
    val $result = ZStream.fromEffect(ZIO.effect("hello world" + Random.nextInt()))
    Cache($result)(List("UseCaseExample", "readAliasStreamFunction"), List(id, key))
  }

  def readStreamFunction(id: Int, key: String): ZStream[Any, Throwable, String] = {
    val $result = ZStream.fromEffect(ZIO.effect("hello world" + Random.nextInt()))
    Cache($result)(List("UseCaseExample", "readStreamFunction"), List(id, key))
  }

  def updateStreamFunction(id: Int, key: String): ZStream[Any, Throwable, String] = {
    val $result = ZStream.fromEffect(ZIO.effect("hello world" + Random.nextInt()))
    Cache.evict($result)(List("readFunction1", "readFunction2")) // not macro, not check whether read function is exists
  }

  @cacheable // caffeine
  def readStreamEntityFunction(id: Int, key: String): ZStream[Any, Throwable, CacheValue] = {
    ZStream.fromEffect(ZIO.effect(CacheValue(Random.nextInt() + "")))
  }

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    (for {
      cache1 <- readStreamEntityFunction(1, "hello-world").runHead
      cache2 <- updateStreamFunction(2, "helloworld").runHead
      _ <- LogUtils.debug(s"${cache1.toString}  ${cache2.toString}")
      _ <- putStrLn("Hello good to meet you!")
    } yield ()).foldM(
      e => LogUtils.debug(s"error => $e").exitCode,
      _ => UIO.effectTotal(ExitCode.success)
    )

}
