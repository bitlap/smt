## @toString

The `@toString` annotation is used to generate `toString` for Scala classes or a `toString` with parameter names for the case classes.

**Note**

- `verbose` Whether to enable detailed log.
- `includeFieldNames` Whether to include the names of the field in the `toString`, default is `true`.
- `includeInternalFields` Whether to include the internal fields defined within a class. Not in a primary constructor, default is `true`.
- `callSuper`             Whether to include the super's `toString`, default is `false`. Not support if super class is a trait.
- Support `case class` and `class`.

**Example**

```scala
@toString class TestClass(val i: Int = 0, var j: Int) {
  val y: Int = 0
  var z: String = "hello"
  var x: String = "world"
}

println(new TestClass(1, 2));
```

Detail options

| includeInternalFields / includeFieldNames | false                                  | true                                             |
| ----------------------------------------- | -------------------------------------- | ------------------------------------------------ |
| false                                     | ```TestClass(1, 2)```                  | ```TestClass(i=0, j=2)```                        |
| true                                      | ```TestClass(1, 2, 0, hello, world)``` | ```TestClass(i=1, j=2, y=0, z=hello, x=world)``` |
