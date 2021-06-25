# scala-macro-tools [![Build](https://github.com/jxnu-liguobin/scala-macro-tools/actions/workflows/ScalaCI.yml/badge.svg)](https://github.com/jxnu-liguobin/scala-macro-tools/actions/workflows/ScalaCI.yml)

Motivation
--

Learn Scala macro and abstract syntax tree.

> The project is currently experimental

# Features

## @toString

The `@toString` used to generate `toString` for Scala classes or a `toString` with parameter names for the case classes.

- Note
  - `verbose` Whether to enable detailed log.
  - `includeFieldNames` Whether to include the names of the field in the `toString`.
  - `includeInternalFields` Whether to include the fields defined within a class. Not in a primary constructor.
  - `callSuper`             Whether to include the super's `toString`. Not support if super class is a trait.
  - Support `case class` and `class`.

- Example

```scala
class TestClass(val i: Int = 0, var j: Int) {
  val y: Int = 0
  var z: String = "hello"
  var x: String = "world"
}

println(new TestClass(1, 2));
```

|includeInternalFields / includeFieldNames| false  |true
|  ---------------------------------  | ----------------------------------  |----------------------------------|
|false|```TestClass(1, 2)``` |```TestClass(i=0, j=2)```|
|true|```TestClass(1, 2, 0, hello, world)```|```TestClass(i=1, j=2, y=0, z=hello, x=world)```|

## @json

The `@json` scala macro annotation is the quickest way to add a JSON format to your Play project's case classes.

- Note
    - This annotation is drawn from [json-annotation](https://github.com/kifi/json-annotation) and have some
      optimization.
    - It can also be used when there are other annotations on the case classes.
    - Only an implicit `val` was generated automatically(Maybe generate a companion object if it not exists), and there are no other
      operations.
- Example

```scala
@json case class Person(name: String, age: Int)
```

You can now serialize/deserialize your objects using Play's convenience methods:

```scala
import play.api.libs.json._

val person = Person("Victor Hugo", 46)
val json = Json.toJson(person)
Json.fromJson[Person](json)
```

## @builder

The `@builder` used to generate builder pattern for Scala classes.

- Note
    - Support `case class` / `class`.
    - It can be used with `@toString`. But `@builder` needs to be put in the back.
    - If there is no companion object, one will be generated to store the `builder` method and `Builder` class.
    - IDE support is not very good, a red prompt will appear, but the compilation is OK.

- Example

```scala
@builder
case class TestClass1(val i: Int = 0, var j: Int, x: String, o: Option[String] = Some(""))

val ret = TestClass1.builder().i(1).j(0).x("x").build()
assert(ret.toString == "TestClass1(1,0,x,Some())")
```

Compiler intermediate code:

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

The `@synchronized` is a more convenient and flexible synchronous annotation.

- Note
  - `lockedName` The name of custom lock obj. 
  - Support static and instance methods.
  - It can customize the lock object, and the default is this.

- Example

```scala

private final val obj = new Object

@synchronized(lockedName = "obj") // The default is this. If you fill in a non existent field name, the compilation will fail.
def getStr3(k: Int): String = {
  k + ""
}

// OR
@synchronized //use this
def getStr(k: Int): String = {
  k + ""
}
```

Compiler intermediate code:

```scala
// Note that it will not judge whether synchronized already exists, so if synchronized already exists, it will be used twice. 
// For example `def getStr(k: Int): String = this.synchronized(this.synchronized(k.$plus("")))
// It is not sure whether it will be optimized at the bytecode level.
def getStr(k: Int): String = this.synchronized(k.$plus(""))
```

# How to use

Add library dependency

```scala
"io.github.jxnu-liguobin" %% "scala-macro-tools" % "<VERSION>"
```

The artefacts have been uploaded to Maven Central.

| Library Version | Scala 2.11 | Scala 2.12 | Scala 2.13 |
|---------|------------|------------|------------|
| 0.0.4   | [![Maven Central](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.11/0.0.4)](https://search.maven.org/artifact/io.github.jxnu-liguobin/scala-macro-tools_2.11/0.0.4/jar)        | [![Maven Central](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.12/0.0.4)](https://search.maven.org/artifact/io.github.jxnu-liguobin/scala-macro-tools_2.12/0.0.4/jar)        | [![Maven Central](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.13/0.0.4)](https://search.maven.org/artifact/io.github.jxnu-liguobin/scala-macro-tools_2.13/0.0.4/jar)        |
| 0.0.3   | [![Maven Central](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.11/0.0.3)](https://search.maven.org/artifact/io.github.jxnu-liguobin/scala-macro-tools_2.11/0.0.3/jar)        | [![Maven Central](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.12/0.0.3)](https://search.maven.org/artifact/io.github.jxnu-liguobin/scala-macro-tools_2.12/0.0.3/jar)        | [![Maven Central](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.13/0.0.3)](https://search.maven.org/artifact/io.github.jxnu-liguobin/scala-macro-tools_2.13/0.0.3/jar)        |
| 0.0.2   | [![Maven Central](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.11/0.0.2)](https://search.maven.org/artifact/io.github.jxnu-liguobin/scala-macro-tools_2.11/0.0.2/jar)        | [![Maven Central](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.12/0.0.2)](https://search.maven.org/artifact/io.github.jxnu-liguobin/scala-macro-tools_2.12/0.0.2/jar)        | [![Maven Central](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.13/0.0.2)](https://search.maven.org/artifact/io.github.jxnu-liguobin/scala-macro-tools_2.13/0.0.2/jar)        |
| 0.0.1   |-|-| [![Maven Central](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.13/0.0.1)](https://search.maven.org/artifact/io.github.jxnu-liguobin/scala-macro-tools_2.13/0.0.1/jar)        |

Importing the library into your build system (e.g gradle, sbt), is not enough. You need to follow an extra step.

| Scala 2.11 | Scala 2.12 | Scala 2.13 |
|------------|-------------|------------|
| Import macro paradise plugin  | Import macro paradise plugin | Enable compiler flag `-Ymacro-annotations` required |

```scala
addCompilerPlugin("org.scalamacros" % "paradise_<your-scala-version>" % "<plugin-version>")
```

Where `<your-scala-version>` must be the full scala version. For example 2.12.13, and not 2.12.

If that doesn't work, google for alternatives.

In version scala`2.13.x`, the functionality of macro paradise has been included in the scala compiler directly. However,
you must still enable the compiler flag `-Ymacro-annotations`.
