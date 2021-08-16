## @toString

`@toString`注解用于为Scala类生成`toString`方法。

**说明**

- `verbose` 指定是否开启详细编译日志。可选，默认`false`。
- `includeFieldNames` 指定是否在`toString`中包含字段的名称。可选，默认`true`。
- `includeInternalFields` 指定是否包含类内部定义的字段。它们不是在主构造函数中。可选，默认`true`。
- `callSuper`             指定是否包含`super`的`toString`方法值。如果超级类是一种特质，则不支持。可选，默认`false`。
- 支持普通类和样例类。

**示例**

```scala
@toString class TestClass(val i: Int = 0, var j: Int) {
  val y: Int = 0
  var z: String = "hello"
  var x: String = "world"
}

println(new TestClass(1, 2));
```

详细选项说明

| includeInternalFields / includeFieldNames | false                                  | true                                             |
| ----------------------------------------- | -------------------------------------- | ------------------------------------------------ |
| false                                     | ```TestClass(1, 2)```                  | ```TestClass(i=0, j=2)```                        |
| true                                      | ```TestClass(1, 2, 0, hello, world)``` | ```TestClass(i=1, j=2, y=0, z=hello, x=world)``` |