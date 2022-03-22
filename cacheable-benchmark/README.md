**环境**

```
[info] # JMH version: 1.32
[info] # VM version: JDK 17.0.1, OpenJDK 64-Bit Server VM, 17.0.1+0
[info] # VM invoker: /usr/local/Cellar/openjdk/17.0.1/libexec/openjdk.jdk/Contents/Home/bin/java
```

**测试方法**

```scala
ZIO.effect {
  Try(Thread.sleep(5)).getOrElse(())
  Random.nextInt() + ""
}
```

**JMH配置**

```
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 5)
@Warmup(iterations = 5)
@Fork(3)
```

**caffeine配置**

```
caffeine {
  maximumSize = 100
  expireAfterWriteSeconds = 60
  disabledLog = true
}
```

**redis配置**

```
redis  {
  host = "0.0.0.0"
  port = 6379
  disabledLog = true
}
```

**结果**

```
[info] Benchmark                                   (limitRandNum)   Mode  Cnt       Score       Error  Units
[info] CacheableBenchmarks.benchmarkCaffeineCache               2  thrpt   15  668084.614 ± 20389.518  ops/s
[info] CacheableBenchmarks.benchmarkNoCache                     2  thrpt   15     164.198 ±     1.656  ops/s
[info] CacheableBenchmarks.benchmarkRedisCache                  2  thrpt   15     663.941 ±   316.381  ops/s
```