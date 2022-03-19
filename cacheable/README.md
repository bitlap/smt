# cacheable

> 基于scala+zio的分布式缓存工具，功能类似Spring缓存注解`@Cacheable`、`@CacheEvict`。

**缓存的语义**
- cacheable --- 查缓存，有则返回，无则查库并设置缓存 
- cacheEvict --- 删除缓存中的某个方法的所有记录
  - values 指定需要清除的方法名 编译器检查，必须为同一个密闭类中，未指定values，则清除该密闭类中的所有缓存

## 如何使用 cacheable

0. 参阅[README_CN#如何使用](../README_CN.md)如何添加依赖。
   1. 确保类路径有以下依赖：
   ```
      "dev.zio" %% "zio-redis" % <VERSION>,
      "com.typesafe" % "config" % <VERSION>,
      "dev.zio" %% "zio" % <VERSION>,
      "dev.zio" %% "zio-schema" % <VERSION>,
      "dev.zio" %% "zio-schema-protobuf" % <VERSION>
    ```

1. 向`application.conf`中添加redis配置，默认配置：

```
redis  {
  host = "0.0.0.0"
  port = 6379
}
```
> resources下没有`application.conf`则使用默认配置，否则使用`application.conf`中的配置

2. 直接使用cacheable模块的`Cache`，支持`ZIO`和`ZStream`：

```scala
  // 注意： 方法的参数用于持久化存储的field，故参数必须都已经重写了`toString`方法
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

> 使用hash存储 key=className-methodName

```scala
@cacheable
def readStreamFunction(id: Int, key: String): ZStream[Any, Throwable, String] = {
  ZStream.fromEffect(ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}"))
}
```

## 如何使用 cacheEvict

> 使用hash存储 key=className-methodName 对应`cacheable` key

1. 直接使用cacheable模块的`Cache`，支持`ZIO`和`ZStream`：

```scala
// 注意： 因为缓存的key是类名+方法名 filed是方法参数，所以该注解将删除所有key的数据，相当于spring的@CacheEvict注解设置allEntries=true
def updateStreamFunction(id: Int, key: String): ZStream[Any, Throwable, String] = {
   val $result = ZStream.fromEffect(ZIO.effect("hello world" + Random.nextInt()))
   // 指定要删除哪些查询方法的缓存？key=className-readFunction1,className-readFunction2
   Cache.evict($result)(List("readFunction1", "readFunction2"))
}

def updateFunction(id: Int, key: String): ZIO[Any, Throwable, String] = {
   val $result = ZIO.effect("hello world" + Random.nextInt())
   Cache.evict($result)(List("readFunction1", "readFunction2"))
}
```

2. 使用`@cacheEvict`注解自动生成:

```scala
// values的值必须是与updateStreamFunction1在一个密闭类中的方法，否则编译不过
// values值为空时，清除以当前类名为前缀的所有缓存：删除哪些缓存？key=className-xx,className-yy,以此类推
@cacheEvict(values = Seq("readStreamFunction1"))
def updateStreamFunction1(id: Int, key: String): ZStream[Any, Throwable, String] = {
   ZStream.fromEffect(ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}"))
}
```

## TODO

- expire 缓存key的过期处理
- 缓存key的构建策略
- 仅删除单个缓存值（hash中的某个field）