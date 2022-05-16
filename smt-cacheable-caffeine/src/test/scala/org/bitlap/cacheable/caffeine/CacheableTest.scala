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
import zio.stream.ZStream
import zio.{ Chunk, Task, ZIO }

import java.util
import scala.util.Random

/** @author
 *    梦境迷离
 *  @since 2021/8/7
 *  @version 1.0
 */
class CacheableTest extends AnyFlatSpec with Matchers {

  val runtime = zio.Runtime.default

  def readIOFunction(id: Int, key: String) = ???

  "cacheable1" should "ok" in {
    @cacheable(local = true)
    def readAliasStreamFunction2(id: Int, key: String): zio.stream.Stream[Throwable, String] =
      ZStream.fromEffect(ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}"))
  }

  "cacheable2" should "ok" in {
    @cacheable(local = true)
    def readFunction(id: Int, key: String): ZIO[Any, Throwable, String] =
      ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}")
  }

  "cacheable3" should "String cannot compile" in {
    """
      |    @cacheable(local = true)
      |    def readFunction(id: Int, key: String): String = {
      |      s"hello world--$id-$key-${Random.nextInt()}"
      |    }
      |""".stripMargin shouldNot compile
  }

  "cacheable4" should "need specified type, otherwise cannot compile" in {
    """
      |    @cacheable(local = true)
      |    def readFunction(id: Int, key: String) = {
      |      s"hello world--$id-$key-${Random.nextInt()}"
      |    }
      |""".stripMargin shouldNot compile
  }

  "cacheable5" should "expected annotation pattern" in {
    @cacheable(local = true)
    def readFunction1(id: Int, key: String): Task[String] =
      ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}")

    @cacheable()
    def readFunction3(id: Int, key: String): Task[String] =
      ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}")
  }

  "cacheable6" should "zio operation is ok with redis" in {
    val cacheValue = Random.nextInt().toString

    @cacheable(true)
    def readIOFunction(id: Int, key: String): ZIO[Any, Throwable, String] =
      ZIO.effect(cacheValue)

    val result = runtime.unsafeRun(for {
      _      <- ZCaffeine.del("CacheableTest-readIOFunction")
      method <- readIOFunction(1, "hello")
      cache  <- ZCaffeine.hGet[String]("CacheableTest-readIOFunction", "1-hello")
    } yield method -> cache)
    Some(result._1) shouldEqual result._2
  }

  // ===================================================================================================================
  // test multi-threads

  case class StoreData(id: String, name: String)

  val globalId = Random.nextInt().toString
  val storeMap = new util.HashMap[String, StoreData]()
  storeMap.put(globalId + "", StoreData(globalId, "zhangsan"))

  "cacheable7" should "test multi-threads" in {
    @cacheable(true)
    def readIOFunction(id: Int): ZStream[Any, Throwable, StoreData] =
      ZStream.fromEffect(
        ZIO.effect(storeMap.get(id + ""))
      )

    @cacheEvict(local = true)
    def updateIOFunction(value: String): ZStream[Any, Throwable, StoreData] =
      ZStream.fromEffect(
        ZIO.effect(storeMap.put(globalId, StoreData(globalId, value)))
      )

    val result = runtime.unsafeRun(for {
      _      <- ZCaffeine.del("CacheableTest-readIOFunction")
      method <- readIOFunction(globalId.toInt).runHead
      update <- updateIOFunction("lisi").runHead
      after  <- readIOFunction(globalId.toInt).runHead
    } yield Some("lisi") -> after.map(_.name))
    result._1 shouldEqual result._2
  }

  "cacheable8" should "test multi-threads" in {
    @cacheable(true)
    def readIOFunction(id: Int): ZStream[Any, Throwable, StoreData] =
      ZStream.fromEffect(
        ZIO.effect(storeMap.get(id + ""))
      )

    @cacheEvict(local = true)
    def updateIOFunction(id: String): ZStream[Any, Throwable, StoreData] =
      ZStream.fromEffect(
        ZIO.effect(storeMap.put(globalId, StoreData(id, "zhangsan")))
      )

    val newId = Random.nextInt().toString
    val result = runtime.unsafeRun(for {
      _      <- ZCaffeine.del("CacheableTest-readIOFunction")
      method <- readIOFunction(globalId.toInt).runHead
      update <- updateIOFunction(newId).runHead
      after  <- readIOFunction(globalId.toInt).runHead
    } yield Some(newId) -> after.map(_.id))
    result._1 shouldEqual result._2
  }

  "cacheable9" should "zio operation is ok with chunk" in {
    val chunk = Chunk(Random.nextInt().toString, Random.nextInt().toString, Random.nextInt().toString)

    @cacheable(local = true)
    def readIOFunction(id: Int, key: String): ZIO[Any, Throwable, Chunk[String]] =
      ZIO.succeed(chunk)

    println(chunk)
    val result = runtime.unsafeRun(for {
      _      <- ZCaffeine.del("CacheableTest-readIOFunction")
      method <- readIOFunction(1, "hello")
      cache  <- ZCaffeine.hGet[Chunk[String]]("CacheableTest-readIOFunction", "1-hello").map(_.getOrElse(Chunk.empty))
    } yield method -> cache)
    result._1 shouldEqual result._2
  }
}
