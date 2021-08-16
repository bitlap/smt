## @equalsAndHashCode

`@equalsAndHashCode`注解用于为普通类生成`equals`和`hashCode`方法，同时均考虑超类的影响。

**说明**
  
- `verbose` 指定是否开启详细编译日志。可选，默认`false`。
- `excludeFields` 指定是否需要排除不需要用于`equals`和`hashCode`方法的字段。可选，默认空（class内部所有非`protected [this]`和`private [this]`的`var、val`字段都将被应用于生成这两个方法）。
- `equals`和`hashCode`方法均会被超类影响，`canEqual`使用`isInstanceOf`，有些人在实现时，使用的是`this.getClass == that.getClass`。
- 采用简单hashCode算法，父类的hashCode是直接被累加的。该算法也是`case class`所使用的。
- 如果注解所在类已经定义了相同签名的`canEqual`方法，则不会生成该方法。
- 包括在类内部中定义的成员字段，在本库中称为内部字段。

**示例**

```scala
@equalsAndHashCode(verbose = true)
class Person(var name: String, var age: Int)
```

**宏生成的中间代码**

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