# cacheable

> 基于scala+zio的分布式缓存工具，功能类似Spring缓存注解`@Cacheable`、`@CacheEvict`。

## 如何使用 cacheable

0. 参阅[README_CN#如何使用](../README_CN.md)如何添加依赖。
   1. 确保类路径有以下依赖：
   ```
      "dev.zio" %% "zio-redis" % <VERSION>,
      "com.typesafe" % "config" % <VERSION>,
      "dev.zio" %% "zio" % "1.0.13" % <VERSION>,
      "dev.zio" %% "zio-schema" % <VERSION>,
      "dev.zio" %% "zio-schema-protobuf" % <VERSION>
    ```

1. 向`application.conf`中添加redis配置, 默认配置：

```
redis  {
  host = "0.0.0.0"
  port = 6379
}
```
> 没有`application.conf`则使用默认配置，否则使用`application.conf`中的配置

2. 直接使用cacheable的`Cache`，支持`ZIO`和`ZStream`：

```scala
  // 注意：  方法的参数用于持久化，故必须都已经重写了`toString`方法
def readStreamFunction(id: Int, key: String): ZStream[Any, Throwable, String] = {
  val $result = ZStream.fromEffect(ZIO.effect("hello world" + Random.nextInt()))
  Cache($result)("UseCaseExample-readStreamFunction", List(id, key)) // "UseCaseExample-readStreamFunction" is hash key
}

def readFunction(id: Int, key: String): ZIO[Any, Throwable, String] = {
  val $result = ZIO.effect("hello world" + Random.nextInt())
  Cache($result)("UseCaseExample-readFunction", List(id, key))
}
```

3. 使用`@cacheable`注解自动生成:

```scala
@cacheable
def readStreamFunction1(id: Int, key: String): ZStream[Any, Throwable, String] = {
  ZStream.fromEffect(ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}"))
}
```

4. TODO

- expire 缓存key的过期处理
- cacheEvict 操作时主动删除缓存
- 缓存key的构建策略
