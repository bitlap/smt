## @builder

`@builder`注解用于为Scala类生成构造器模式。

**说明**

- 支持普通类和样例类。
- 仅支持对主构造函数使用。
- 如果该类没有伴生对象，将生成一个伴生对象来存储`builder`方法和类。

**示例**

```scala
@builder
case class TestClass1(val i: Int = 0, var j: Int, x: String, o: Option[String] = Some(""))

val ret = TestClass1.builder().i(1).j(0).x("x").build()
assert(ret.toString == "TestClass1(1,0,x,Some())")
```

**宏生成的中间代码**

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