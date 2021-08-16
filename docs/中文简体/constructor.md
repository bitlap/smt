## @constructor

`@constructor`注解用于为普通类生成辅助构造函数。仅当类有内部字段时可用。

**说明**
  
- `verbose` 指定是否开启详细编译日志。可选，默认`false`。
- `excludeFields` 指定是否需要排除不需要用于构造函数的`var`字段。可选，默认空（所有class内部的`var`字段都将作为构造函数的入参）。
- 仅支持在`class`上使用。
- 主构造函数存在柯里化时，内部字段被放置在柯里化的第一个括号块中。（生成的仍然是柯里化的辅助构造）
- 内部字段的类型需要显示指定，否则宏拓展无法获取到该类型。目前支持为基本类型和字符串实现省略。如`var i = 1; var j: Int = 1; var k: Object = new Object()`都是可以的，而`var k = new Object()`是不可以的。

**示例**

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

**宏生成的中间代码**

仅构造函数部分

```scala
def <init>(int: Int, j: Int, k: Option[String], t: Option[Long], b: Int) = {
  <init>(int, j, k, t);
  this.b = b
}
```