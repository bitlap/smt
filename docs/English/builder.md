## @builder

The `@builder` annotation is used to generate builder pattern for Scala classes.

**Note**

- Support `case class` / `class`.
- Only support for **primary constructor**.
- If there is no companion object, one will be generated to store the `builder` class and method.

**Example**

```scala
@builder
case class TestClass1(val i: Int = 0, var j: Int, x: String, o: Option[String] = Some(""))

val ret = TestClass1.builder().i(1).j(0).x("x").build()
assert(ret.toString == "TestClass1(1,0,x,Some())")
```

**Macro expansion code**

```scala
object TestClass1 extends scala.AnyRef {
  def <init>() = {
    super.<init>();
    ()
  };
  def builder(): TestClass1Builder = new TestClass1Builder();
  class TestClass1Builder extends scala.AnyRef {
    def <init>() = {
      super.<init>();
      ()
    };
    private var i: Int = 0;
    private var j: Int = _;
    private var x: String = _;
    private var o: Option[String] = Some("");
    def i(i: Int): TestClass1Builder = {
      this.i = i;
      this
    };
    def j(j: Int): TestClass1Builder = {
      this.j = j;
      this
    };
    def x(x: String): TestClass1Builder = {
      this.x = x;
      this
    };
    def o(o: Option[String]): TestClass1Builder = {
      this.o = o;
      this
    };
    def build(): TestClass1 = TestClass1(i, j, x, o)
  }
}
```