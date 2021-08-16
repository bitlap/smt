## @constructor

The `@constructor` annotation is used to generate secondary constructor method for classes, only when it has internal fields.

**Note**

- `verbose` Whether to enable detailed log.
- `excludeFields` Whether to exclude the specified `var` fields, default is `Nil`.
- Only support `class`.
- The internal fields are placed in the first bracket block if constructor is currying.
- The type of the internal field must be specified, otherwise the macro extension cannot get the type.
  At present, only primitive types and string can be omitted. For example, `var i = 1; var j: int = 1; var k: Object = new Object()` is OK, but `var k = new object()` is not.

**Example**

```scala
@constructor(excludeFields = Seq("c"))
class A2(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L)) {
  private val a: Int = 1
  var b: Int = 1 //The default value of the field is not carried to the apply parameter, so all parameters are required.
  protected var c: Int = _

  def helloWorld: String = "hello world"
}

println(new A2(1, 2, None, None, 100))
```

**Macro expansion code**

Only constructor

```scala
def <init>(int: Int, j: Int, k: Option[String], t: Option[Long], b: Int) = {
  <init>(int, j, k, t);
  this.b = b
}
```