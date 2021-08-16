## @json

The `@json` annotation is the quickest way to add a JSON format to your Play project's case classes.

**Note**

- This annotation is drawn from [json-annotation](https://github.com/kifi/json-annotation) and have some
  optimization.
- It can also be used when there are other annotations on the case classes.
- Only an implicit `val` was generated automatically(Maybe generate a companion object if it not exists), and there are no other
  operations.

**Example**

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