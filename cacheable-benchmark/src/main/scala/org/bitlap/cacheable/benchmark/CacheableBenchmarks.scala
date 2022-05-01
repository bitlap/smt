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

package org.bitlap.cacheable.benchmark

import scala.util.Try

import org.bitlap.cacheable.core.cacheable
import org.openjdk.jmh.annotations._
import zio.ZIO

import java.util.concurrent.TimeUnit
import scala.util.Random

/**
 * benchmark @cacheable
 *
 * @author 梦境迷离
 * @version 1.0,2022/3/22
 */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 5)
@Warmup(iterations = 5)
@Fork(3)
class CacheableBenchmarks extends BenchmarkRuntime {

  // make it use cache only sometimes
  // Smaller numbers are easier to hit the cache
  // 25% hit cache
  @Param(Array("2"))
  var limitRandNum: Int = _

  @Benchmark
  def benchmarkRedisCache(): Unit =
    execute[String](cacheableRedis(Random.nextInt(limitRandNum), Random.nextInt(limitRandNum) + ""))

  @Benchmark
  def benchmarkCaffeineCache(): Unit =
    execute[String](cacheableCaffeine(Random.nextInt(limitRandNum), Random.nextInt(limitRandNum) + ""))

  @Benchmark
  def benchmarkNoCache(): Unit =
    execute[String](unCacheable(Random.nextInt(limitRandNum), Random.nextInt(limitRandNum) + ""))

  @cacheable(local = false) // use RedisExecutor.live, not RedisExecutor.local
  @inline def cacheableRedis(id: Int, key: String): ZIO[Any, Throwable, String] =
    ZIO.effect {
      Try(Thread.sleep(5)).getOrElse(()) // Simulate a JDBC request
      Random.nextInt() + ""
    }

  @cacheable(local = true)
  @inline def cacheableCaffeine(id: Int, key: String): ZIO[Any, Throwable, String] =
    ZIO.effect {
      Try(Thread.sleep(5)).getOrElse(())
      Random.nextInt() + ""
    }

  @inline def unCacheable(id: Int, key: String): ZIO[Any, Throwable, String] =
    ZIO.effect {
      Try(Thread.sleep(5)).getOrElse(())
      Random.nextInt() + ""
    }
}
