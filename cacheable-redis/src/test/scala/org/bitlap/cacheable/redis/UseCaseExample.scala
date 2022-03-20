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

import org.bitlap.cacheable.core.{ Cache, LogUtils }
import org.bitlap.cacheable.redis.Implicits._
import zio.console.putStrLn
import zio.stream.ZStream
import zio.{ ExitCode, UIO, URIO, ZIO }

import scala.util.Random

/**
 * use these function to test it.
 *
 * @author 梦境迷离
 * @version 1.0,2022/3/18
 */
object UseCaseExample extends zio.App {

  def readAliasStreamFunction(id: Int, key: String): zio.stream.Stream[Throwable, String] = {
    val $result = ZStream.fromEffect(ZIO.effect("hello world" + Random.nextInt()))
    Cache($result)(List("UseCaseExample", "readAliasStreamFunction"), List(id, key))
  }

  def readStreamFunction(id: Int, key: String): ZStream[Any, Throwable, String] = {
    val $result = ZStream.fromEffect(ZIO.effect("hello world" + Random.nextInt()))
    Cache($result)(List("UseCaseExample", "readStreamFunction"), List(id, key))
  }

  def readFunction(id: Int, key: String): ZIO[Any, Throwable, String] = {
    val $result = ZIO.effect("hello world" + Random.nextInt())
    Cache($result)(List("UseCaseExample", "readFunction"), List(id, key))
  }

  def updateStreamFunction(id: Int, key: String): ZStream[Any, Throwable, String] = {
    val $result = ZStream.fromEffect(ZIO.effect("hello world" + Random.nextInt()))
    Cache.evict($result)(List("readFunction1", "readFunction2"))
  }

  def updateFunction(id: Int, key: String): ZIO[Any, Throwable, String] = {
    val $result = ZIO.effect("hello world" + Random.nextInt())
    Cache.evict($result)(List("readFunction1", "readFunction2"))
  }

  def readEntityFunction(id: Int, key: String): ZIO[Any, Throwable, CacheValue] = {
    val $result = ZIO.effect(CacheValue(Random.nextInt() + ""))
    Cache($result)(List("UseCaseExample", "readEntityFunction"), List(id, key))
  }

  def readStreamEntityFunction(id: Int, key: String): ZStream[Any, Throwable, CacheValue] = {
    val $result = ZStream.fromEffect(ZIO.effect(CacheValue(Random.nextInt() + "")))
    Cache($result)(List("UseCaseExample", "readStreamEntityFunction"), List(id, key))
  }

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    (for {
      ret <- readStreamEntityFunction(1, "hello-world").runHead
      cache = zio.Runtime.default.unsafeRun(ZRedisService.hGet[CacheValue]("UseCaseExample-readStreamEntityFunction", "1-hello-world"))
      _ <- LogUtils.debug(s"${ret.toString}    $cache")
      _ <- putStrLn("Hello good to meet you!")
    } yield ()).foldM(
      e => LogUtils.debug(s"error => $e").exitCode,
      _ => UIO.effectTotal(ExitCode.success)
    )

}