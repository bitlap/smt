# cacheable

> A distributed cache based on scala macro annotation

## How to use library

see [README#How to use](../README.md)

1. add redis config into `application.conf`, default config:

```
redis  {
  host = "0.0.0.0"
  port = 6379
}
```

2. use cacheable directly by `Cache`:

```scala
  // NOTE  all arguments should override `toString`
def readStreamFunction(id: Int, key: String): ZStream[Any, Throwable, String] = {
  val $result = ZStream.fromEffect(ZIO.effect("hello world" + Random.nextInt()))
  Cache($result)("UseCaseExample-readStreamFunction", List(id, key))
}

def readFunction(id: Int, key: String): ZIO[Any, Throwable, String] = {
  val $result = ZIO.effect("hello world" + Random.nextInt())
  Cache($result)("UseCaseExample-readFunction", List(id, key))
}
```

3. use `@cacheable`:

```scala
    @cacheable
def readStreamFunction1(id: Int, key: String): ZStream[Any, Throwable, String] = {
  ZStream.fromEffect(ZIO.effect(s"hello world--$id-$key-${Random.nextInt()}"))
}
```

4. TODO

- expire
- cacheput
- cacheevict
- the strategy of generating key