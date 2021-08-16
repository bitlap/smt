## @elapsed

`@elapsed`注解用于计算方法的执行耗时

**说明**
  
- `limit` 执行耗时超过该值则打印日志或输出到控制台。
  - 方法的所有者作用域内有`slf4j`的`org.slf4j.Logger`对象，则使用该对象，否则使用`println`。
- `logLevel` 指定打印的日志级别。
- 支持方法的返回类型为`Future[_]`。
  - 使用`map`实现。
- 支持方法的返回类型的不是`Future`。
  - 使用`try finally`实现。
- 仅能在非抽象方法上使用该注解。

**示例**

```scala
class A {
  // Duration和TimeUnit必须是全类名
  @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = io.github.dreamylost.LogLevel.WARN)
  def helloScala1(t: String): Future[String] = {
    Future(t)(scala.concurrent.ExecutionContext.Implicits.global)
  }

  @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = io.github.dreamylost.LogLevel.INFO)
  def helloScala2: String = Await.result(helloScala1("world"), Duration.Inf)
}
```