# scala-macro-tools [![Scala CI](https://github.com/jxnu-liguobin/scala-macro-tools/actions/workflows/ScalaCI.yml/badge.svg)](https://github.com/jxnu-liguobin/scala-macro-tools/actions/workflows/ScalaCI.yml)

Motivation
--

scala macro and abstract syntax tree learning code.

# @toString

- Argument
    - `verbose` Whether to enable detailed log.
    - `withFieldName` Whether to include the name of the field in the toString.
    - `withInternalField` Whether to include the fields defined within a class.
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

|withInternalField / withFieldName| false  |true
|  ----  | ----  |----|
|false|```TestClass(1, 2)``` |```TestClass(i=0, j=2)```|
|true|```TestClass(1, 2, 0, hello, world)```|```TestClass(i=1, j=2, y=0, z=hello, x=world)```|