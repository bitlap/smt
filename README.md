# scala-macro-tools

scala macro and abstract syntax tree learning code.

# @toString

- Argument
    - `verbose` Whether to enable detailed log.
    - `withFieldName` Whether to include the name of the field in the toString.
    - `containsCtorParams` Whether to include the fields of the primary constructor.

- source code1

```scala
class TestClass(val i: Int = 0, var j: Int) {
  val y: Int = 0
  var z: String = "hello"
  var x: String = "world"
}

case class TestClass2(i: Int = 0, var j: Int) // No method body, only have primary constructor.
```

- when withFieldName=false containsCtorParams=false

```
println(new TestClass(1, 2))
TestClass(0, hello, world)
```

- when withFieldName=false containsCtorParams=true

```
println(new TestClass(1, 2))
TestClass(1, 2, 0, hello, world)
```

- when withFieldName=true containsCtorParams=false

```
println(new TestClass(1, 2))
TestClass(y=0, z=hello, x=world)
```

- when withFieldName=true containsCtorParams=true

```
println(new TestClass(1, 2))
TestClass(i=1, j=2, y=0, z=hello, x=world)
```