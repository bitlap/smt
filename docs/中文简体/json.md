## @json

`@json`注解是向Play项目的样例类添加json format对象的最快方法。

**说明**

- 此注释启发来自[json-annotation](https://github.com/kifi/json-annotation)，并做了优化，现在它可以与其他注解同时使用。
- 只有一个隐式的`val`值会被自动生成（如果伴生对象不存在的话，还会生成一个伴生对象用于存放该隐式值），此外没有其他的操作。

**示例**

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