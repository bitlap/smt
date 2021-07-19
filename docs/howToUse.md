## @toString

`@toString`注解用于为Scala类生成`toString`方法。

- 说明
    - `verbose` 指定是否开启详细编译日志。可选，默认`false`。
    - `includeFieldNames` 指定是否在`toString`中包含字段的名称。可选，默认`true`。
    - `includeInternalFields` 指定是否包含类内部定义的字段。它们不是在主构造函数中。可选，默认`true`。
    - `callSuper`             指定是否包含`super`的`toString`方法值。如果超级类是一种特质，则不支持。可选，默认`false`。
    - 支持普通类和样例类。

- 示例

```scala
@toString class TestClass(val i: Int = 0, var j: Int) {
  val y: Int = 0
  var z: String = "hello"
  var x: String = "world"
}

println(new TestClass(1, 2));
```

| includeInternalFields / includeFieldNames | false                                  | true                                             |
| ----------------------------------------- | -------------------------------------- | ------------------------------------------------ |
| false                                     | ```TestClass(1, 2)```                  | ```TestClass(i=0, j=2)```                        |
| true                                      | ```TestClass(1, 2, 0, hello, world)``` | ```TestClass(i=1, j=2, y=0, z=hello, x=world)``` |

## @json

`@json`注解是向Play项目的样例类添加json format对象的最快方法。

- 说明
    - 此注释启发来自[json-annotation](https://github.com/kifi/json-annotation)，并做了优化，现在它可以与其他注解同时使用。
    - 只有一个隐式的`val`值会被自动生成（如果伴生对象不存在的话，还会生成一个伴生对象用于存放该隐式值），此外没有其他的操作。

- 示例

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

## @builder

`@builder`注解用于为Scala类生成构造器模式。

- 说明
    - 支持普通类和样例类。
    - 仅支持对主构造函数使用。
    - 如果该类没有伴生对象，将生成一个伴生对象来存储`builder`方法和类。

- 示例

```scala
@builder
case class TestClass1(val i: Int = 0, var j: Int, x: String, o: Option[String] = Some(""))

val ret = TestClass1.builder().i(1).j(0).x("x").build()
assert(ret.toString == "TestClass1(1,0,x,Some())")
```

宏生成的中间代码：

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

## @synchronized

`@synchronized`注解是一个更方便、更灵活的用于同步方法的注解。

- 说明
    - `lockedName` 指定自定义的锁对象的名称。可选，默认`this`。
    - 支持静态方法（`object`中的函数）和实例方法（`class`中的函数）。

- 示例

```scala

private final val obj = new Object

@synchronized(lockedName = "obj") // 如果您填写一个不存在的字段名，编译将失败。
def getStr3(k: Int): String = {
  k + ""
}

// 或者
@synchronized //使用 this 作为锁对象
def getStr(k: Int): String = {
  k + ""
}
```

宏生成的中间代码：

```scala
// 注意，它不会判断synchronized是否已经存在，因此如果synchronized已经存在，它将被使用两次。如下 
// `def getStr(k: Int): String = this.synchronized(this.synchronized(k.$plus("")))
// 目前还不确定是否在字节码级别会被优化。
def getStr(k: Int): String = this.synchronized(k.$plus(""))
```

## @log

`@log`注解不使用混入和包装，而是直接使用宏生成默认的log对象来操作log。

- 说明
    - `verbose` 指定是否开启详细编译日志。可选，默认`false`。
    - `logType` 指定需要生成的`log`的类型。可选，默认`JLog`
        - `io.github.dreamylost.logs.LogType.JLog` 使用 `java.util.logging.Logger`
        - `io.github.dreamylost.logs.LogType.Log4j2` 使用 `org.apache.logging.log4j.Logger`
        - `io.github.dreamylost.logs.LogType.Slf4j` 使用 `org.slf4j.Logger`
    - 支持普通类，样例类，单例对象。

- 示例

```scala
@log(verbose = true) class TestClass1(val i: Int = 0, var j: Int) {
  log.info("hello")
}

@log(verbose=true, logType=io.github.dreamylost.LogType.Slf4j) class TestClass6(val i: Int = 0, var j: Int){ log.info("hello world") }
```

## @apply

`@apply`注解用于为普通类的主构造函数生成`apply`方法。

- 说明
    - `verbose` 指定是否开启详细编译日志。可选，默认`false`。
    - 仅支持在`class`上使用且仅支持主构造函数。

- 示例

```scala
@apply @toString class B2(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L))
println(B2(1, 2, None, None)) //0.1.0，不携带字段的默认值到apply参数中，所以参数都是必传
```

## @constructor

`@constructor`注解用于为普通类生成辅助构造函数。仅当类有内部字段时可用。

- 说明
    - `verbose` 指定是否开启详细编译日志。可选，默认`false`。
    - `excludeFields` 指定是否需要排除不需要用于构造函数的`var`字段。可选，默认空（所有class内部的`var`字段都将作为构造函数的入参）。
    - 仅支持在`class`上使用。
    - 主构造函数存在柯里化时，内部字段被放置在柯里化的第一个括号块中。（生成的仍然是柯里化的辅助构造）
    - 内部字段的类型需要显示指定，否则宏拓展无法获取到该类型。目前支持为基本类型和字符串实现省略。如`var i = 1; var j: Int = 1; var k: Object = new Object()`都是可以的，而`var k = new Object()`是不可以的。

- 示例

```scala
@constructor(excludeFields = Seq("c")) //排除c字段。其中，a是val的不需要手动指定，自动排除。
class A2(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L)) {
  private val a: Int = 1
  var b: Int = 1 // 不携带字段的默认值到apply参数中，所以参数都是必传
  protected var c: Int = _

  def helloWorld: String = "hello world"
}

println(new A2(1, 2, None, None, 100))
```

宏生成的中间代码（仅构造函数部分）：

```scala
def <init>(int: Int, j: Int, k: Option[String], t: Option[Long], b: Int) = {
  <init>(int, j, k, t);
  this.b = b
}
```

## @equalsAndHashCode

`@equalsAndHashCode`注解用于为普通类生成`equals`和`hashCode`方法，同时均考虑超类的影响。

- 说明
  - `verbose` 指定是否开启详细编译日志。可选，默认`false`。
  - `excludeFields` 指定是否需要排除不需要用于`equals`和`hashCode`方法的字段。可选，默认空（class内部所有非私有的`var、val`字段都将被应用于生成这两个方法）。
  - `equals`和`hashCode`方法均会被超类影响，`canEqual`使用`isInstanceOf`，有些人在实现时，使用的是`this.getClass == that.getClass`。
  - 采用简单hashCode算法，父类的hashCode是直接被累加的。该算法是`case class`使用的。

- 示例

```scala
@equalsAndHashCode(verbose = true)
class Person(var name: String, var age: Int)
```

宏生成的中间代码：

```scala
class Person extends scala.AnyRef {
  <paramaccessor> var name: String = _;
  <paramaccessor> var age: Int = _;
  def <init>(name: String, age: Int) = {
    super.<init>();
    ()
  };
  def canEqual(that: Any) = that.isInstanceOf[Person];
  override def equals(that: Any): Boolean = that match {
    case (t @ (_: Person)) => t.canEqual(this).$amp$amp(Seq(this.name.equals(t.name), this.age.equals(t.age)).forall(((f) => f))).$amp$amp(true)
    case _ => false
  };
  override def hashCode(): Int = {
    val state = Seq(name, age);
    state.map(((x$2) => x$2.hashCode())).foldLeft(0)(((a, b) => 31.$times(a).$plus(b)))
  }
}
```  