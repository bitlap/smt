## @log

The `@log` annotation does not use mixed or wrapper, but directly uses macro to generate default log object and operate log. (Log dependency needs to be introduced)

**Note**

- `verbose` Whether to enable detailed log.
- `logType` Specifies the type of `log` that needs to be generated, default is `io.github.dreamylost.logs.LogType.JLog`.
    - `io.github.dreamylost.logs.LogType.JLog` use `java.util.logging.Logger`
    - `io.github.dreamylost.logs.LogType.Log4j2` use `org.apache.logging.log4j.Logger`
    - `io.github.dreamylost.logs.LogType.Slf4j` use `org.slf4j.Logger`
    - `io.github.dreamylost.logs.LogType.ScalaLoggingLazy` implement by `scalalogging.LazyLogging` but field was renamed to `log`
    - `io.github.dreamylost.logs.LogType.ScalaLoggingStrict` implement by `scalalogging.StrictLogging` but field was renamed to `log`
- Support `class` and `object`.

**Example**

```scala
@log(verbose = true) class TestClass1(val i: Int = 0, var j: Int) {
  log.info("hello")
}

@log(verbose=true, logType=io.github.dreamylost.logs.LogType.Slf4j) 
class TestClass6(val i: Int = 0, var j: Int) { 
  log.info("hello world") 
}
```