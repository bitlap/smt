# scala-macro-tools [![Build](https://github.com/jxnu-liguobin/scala-macro-tools/actions/workflows/ScalaCI.yml/badge.svg)](https://github.com/jxnu-liguobin/scala-macro-tools/actions/workflows/ScalaCI.yml)

我写该库的动机
--

学习Scala宏编程（macro）和抽象语法树（ast）。

> 本项目目前处于实验阶段

[中文说明](./README.md)|[English](./README_EN.md)


# 功能

- `@toString`
- `@json`
- `@builder`
- `@synchronized`
- `@log`
- `@apply`

## @toString

`@toString`注解用于为Scala类生成`toString`方法。

- 说明
  - `verbose` 指定是否开启详细编译日志。可选，默认`false`。
  - `includeFieldNames` 指定是否在`toString`中包含字段的名称。可选，默认`true`。
  - `includeInternalFields` 指定是否包含类内部定义的字段。它们不是在主构造函数中。可选，默认`true`。
  - `callSuper`             指定是否包含`super`的`toString`方法值。如果超级类是一种特质，则不支持。可选，默认`false`。
  - 支持普通类和样例类。

- 示例

```scala
class TestClass(val i: Int = 0, var j: Int) {
  val y: Int = 0
  var z: String = "hello"
  var x: String = "world"
}

println(new TestClass(1, 2));
```

| includeInternalFields / includeFieldNames | false                                  | true                                             |
| ----------------------------------------- | -------------------------------------- | ------------------------------------------------ |
| false                                     | ```TestClass(1, 2)```                  | ```TestClass(i=0, j=2)```                        |
| true                                      | ```TestClass(1, 2, 0, hello, world)``` | ```TestClass(i=1, j=2, y=0, z=hello, x=world)``` |

## @json

`@json`注解是向Play项目的样例类添加json format对象的最快方法。

- 说明
    - 此注释启发来自[json-annotation](https://github.com/kifi/json-annotation)，并做了优化，现在它可以与其他注解同时使用。
    - 只有一个隐式的`val`值会被自动生成（如果伴生对象不存在的话，还会生成一个伴生对象用于存放该隐式值），此外没有其他的操作。

- 示例

```scala
@json case class Person(name: String, age: Int)
```

现在，您可以使用Play的转化方法序列化或反序列化对象：

```scala
import play.api.libs.json._

val person = Person("Victor Hugo", 46)
val json = Json.toJson(person)
Json.fromJson[Person](json)
```

## @builder

`@builder`注解用于为Scala类生成构造器模式。

- 说明
    - 支持普通类和样例类。
    - 如果该类没有伴生对象，将生成一个伴生对象来存储`builder`方法和类。
    - 目前不支持主构造函数是柯里化的。
    
> IDEA对宏的支持不是很好，所以会出现标红，不过编译没问题，调用结果也符合预期。这意味着，目前不支持语法提示。

- 示例

```scala
@builder
case class TestClass1(val i: Int = 0, var j: Int, x: String, o: Option[String] = Some(""))

val ret = TestClass1.builder().i(1).j(0).x("x").build()
assert(ret.toString == "TestClass1(1,0,x,Some())")
```

宏生成的中间代码

```scala
object TestClass1 extends scala.AnyRef {
  def <init>() = {
    super.<init>();
    ()
  };
  def builder(): Builder = new Builder();
  class Builder extends scala.AnyRef {
    def <init>() = {
      super.<init>();
      ()
    };
    private var i: Int = 0;
    private var j: Int = _;
    private var x: String = _;
    private var o: Option[String] = Some("");
    def i(i: Int): Builder = {
      this.i = i;
      this
    };
    def j(j: Int): Builder = {
      this.j = j;
      this
    };
    def x(x: String): Builder = {
      this.x = x;
      this
    };
    def o(o: Option[String]): Builder = {
      this.o = o;
      this
    };
    def build(): TestClass1 = TestClass1(i, j, x, o)
  }
}
```

## @synchronized

`@synchronized`注解是一个更方便、更灵活的用于同步方法的注解。

- 说明
  - `lockedName` 指定自定义的锁对象的名称。可选，默认`this`。 
  - 支持静态方法（`object`中的函数）和实例方法（`class`中的函数）。

- 示例

```scala

private final val obj = new Object

@synchronized(lockedName = "obj") // 如果您填写一个不存在的字段名，编译将失败。
def getStr3(k: Int): String = {
  k + ""
}

// 或者
@synchronized //使用 this 作为锁对象
def getStr(k: Int): String = {
  k + ""
}
```

Compiler intermediate code:

```scala
// 注意，它不会判断synchronized是否已经存在，因此如果synchronized已经存在，它将被使用两次。如下 
// `def getStr(k: Int): String = this.synchronized(this.synchronized(k.$plus("")))
// 目前还不确定是否在字节码级别会被优化。
def getStr(k: Int): String = this.synchronized(k.$plus(""))
```

## @log

`@log`注解不使用混入和包装，而是直接使用宏生成默认的log对象来操作log。

- 说明
  - `verbose` 指定是否开启详细编译日志。可选，默认`false`。
  - `logType` 指定需要生成的`log`的类型。可选，默认`JLog`
    - `io.github.dreamylost.LogType.JLog` 使用 `java.util.logging.Logger`
    - `io.github.dreamylost.LogType.Log4j2` 使用 `org.apache.logging.log4j.Logger`
    - `io.github.dreamylost.LogType.Slf4j` 使用 `org.slf4j.Logger`
  - 支持普通类，样例类，单例对象。
    

> IDEA对宏的支持不是很好，所以会出现标红，不过编译没问题，调用结果也符合预期。这意味着，目前不支持语法提示。

- 示例

```scala
@log(verbose = true) class TestClass1(val i: Int = 0, var j: Int) {
  log.info("hello")
}

@log(verbose=true, logType=io.github.dreamylost.LogType.Slf4j) class TestClass6(val i: Int = 0, var j: Int){ log.info("hello world") }
```

## @apply

`@apply`注解用于为普通类的主构造函数生成`apply`方法。

- 说明
  - `verbose` 指定是否开启详细编译日志。可选，默认`false`。
  - 仅支持在`class`上使用。
  - 仅支持主构造函数。
  - 目前不支持主构造函数是柯里化的。

> IDEA对宏的支持不是很好，所以会出现标红，不过编译没问题，调用结果也符合预期。这意味着，目前不支持语法提示。

- 示例

```scala
@apply @toString class B2(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L))
println(B2(1, 2))
```

# 如何使用

添加库依赖，在sbt中

> 在gradle，maven中，通常`scala-macro-tools`被替换为`scala-macro-tools_2.12`这种。其中，`2.12`表示Scala版本号。

```scala
"io.github.jxnu-liguobin" %% "scala-macro-tools" % "<VERSION>"
```

该库已发布到maven中央仓库，请使用最新版本。

| Library Version | Scala 2.11                                                                                                                                                                                                  | Scala 2.12                                                                                                                                                                                                  | Scala 2.13                                                                                                                                                                                                  |
| --------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 0.0.6           | [![Maven Central](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.11/0.0.6)](https://search.maven.org/artifact/io.github.jxnu-liguobin/scala-macro-tools_2.11/0.0.6/jar) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.12/0.0.6)](https://search.maven.org/artifact/io.github.jxnu-liguobin/scala-macro-tools_2.12/0.0.6/jar) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.13/0.0.6)](https://search.maven.org/artifact/io.github.jxnu-liguobin/scala-macro-tools_2.13/0.0.6/jar) |
| 0.0.5           | [![Maven Central](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.11/0.0.5)](https://search.maven.org/artifact/io.github.jxnu-liguobin/scala-macro-tools_2.11/0.0.5/jar) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.12/0.0.5)](https://search.maven.org/artifact/io.github.jxnu-liguobin/scala-macro-tools_2.12/0.0.5/jar) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.13/0.0.5)](https://search.maven.org/artifact/io.github.jxnu-liguobin/scala-macro-tools_2.13/0.0.5/jar) |
| 0.0.4           | [![Maven Central](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.11/0.0.4)](https://search.maven.org/artifact/io.github.jxnu-liguobin/scala-macro-tools_2.11/0.0.4/jar) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.12/0.0.4)](https://search.maven.org/artifact/io.github.jxnu-liguobin/scala-macro-tools_2.12/0.0.4/jar) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.13/0.0.4)](https://search.maven.org/artifact/io.github.jxnu-liguobin/scala-macro-tools_2.13/0.0.4/jar) |
| 0.0.3           | [![Maven Central](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.11/0.0.3)](https://search.maven.org/artifact/io.github.jxnu-liguobin/scala-macro-tools_2.11/0.0.3/jar) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.12/0.0.3)](https://search.maven.org/artifact/io.github.jxnu-liguobin/scala-macro-tools_2.12/0.0.3/jar) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.13/0.0.3)](https://search.maven.org/artifact/io.github.jxnu-liguobin/scala-macro-tools_2.13/0.0.3/jar) |
| 0.0.2           | [![Maven Central](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.11/0.0.2)](https://search.maven.org/artifact/io.github.jxnu-liguobin/scala-macro-tools_2.11/0.0.2/jar) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.12/0.0.2)](https://search.maven.org/artifact/io.github.jxnu-liguobin/scala-macro-tools_2.12/0.0.2/jar) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.13/0.0.2)](https://search.maven.org/artifact/io.github.jxnu-liguobin/scala-macro-tools_2.13/0.0.2/jar) |
| 0.0.1           | -                                                                                                                                                                                                           | -                                                                                                                                                                                                           | [![Maven Central](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.13/0.0.1)](https://search.maven.org/artifact/io.github.jxnu-liguobin/scala-macro-tools_2.13/0.0.1/jar) |

仅将本库导入构建系统（例如gradle、sbt）是不够的。你需要多走一步。

| Scala 2.11               | Scala 2.12               | Scala 2.13                            |
| ------------------------ | ------------------------ | ------------------------------------- |
| 导入 macro paradise 插件 | 导入 macro paradise 插件 | 开启 编译器标记 `-Ymacro-annotations` |

```scala
addCompilerPlugin("org.scalamacros" % "paradise_<your-scala-version>" % "<plugin-version>")
```

`<your-scala-version>`必须是Scala版本号的完整编号，如`2.12.13`，而不是`2.12`。

如果这不起作用，可以谷歌寻找替代品。

在`scala 2.13.x`版本中，macro paradise的功能直接包含在scala编译器中。然而，仍然必须启用编译器标志`-Ymacro annotations`。
