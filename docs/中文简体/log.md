## @log

`@log`注解不使用混入和包装，而是直接使用宏生成默认的log对象来操作log。日志库的依赖需要自己引入。

**说明**

- `verbose` 指定是否开启详细编译日志。可选，默认`false`。
- `logType` 指定需要生成的`log`的类型。可选，默认`io.github.dreamylost.logs.LogType.JLog`。
  - `io.github.dreamylost.logs.LogType.JLog` 使用 `java.util.logging.Logger`
  - `io.github.dreamylost.logs.LogType.Log4j2` 使用 `org.apache.logging.log4j.Logger`
  - `io.github.dreamylost.logs.LogType.Slf4j` 使用 `org.slf4j.Logger`
  - `io.github.dreamylost.logs.LogType.ScalaLoggingLazy` 基于 `scalalogging.LazyLogging` 实现，但字段被重命名为`log`
  - `io.github.dreamylost.logs.LogType.ScalaLoggingStrict` 基于 `scalalogging.StrictLogging`实现， 但字段被重命名为`log`
- 支持普通类，单例对象。

**示例**

```scala
@log(verbose = true) class TestClass1(val i: Int = 0, var j: Int) {
  log.info("hello")
}

@log(verbose=true, logType=io.github.dreamylost.logs.LogType.Slf4j) 
class TestClass6(val i: Int = 0, var j: Int) { 
  log.info("hello world") 
}
```