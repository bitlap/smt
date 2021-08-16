## @elapsed

The `@elapsed` annotation is used to calculate the execution time of the method.

**Note**

- `limit` The log will be printed or output to the console if the execution time exceeds this value.
    - If there is an `org.slf4j.Logger` object of `slf4j` in the owner scope of the method, this object is used; otherwise, `println` is used.
- `logLevel` Specifies the log level to print.
- The return type of supported method is not `Future[_]`.
    - Use `map` to implement.
- The return type of the supported method is not `Future`.
    - Use `try finally` to implement.
- Annotation is only supported use on non-abstract method.

**Example**

```scala
class A {
  // Duration and TimeUnit must Full class name
  @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = io.github.dreamylost.LogLevel.WARN)
  def helloScala1(t: String): Future[String] = {
    Future(t)(scala.concurrent.ExecutionContext.Implicits.global)
  }

  @elapsed(limit = scala.concurrent.duration.Duration(1, java.util.concurrent.TimeUnit.SECONDS), logLevel = io.github.dreamylost.LogLevel.INFO)
  def helloScala2: String = Await.result(helloScala1("world"), Duration.Inf)
}
```