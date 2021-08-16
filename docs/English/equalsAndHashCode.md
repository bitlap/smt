## @equalsAndHashCode

The `@equalsAndHashCode` annotation is used to generate `equals` and `hashCode` methods for ordinary classes, and them takes into account the influence of super classes.

**Note**

- `verbose` Whether to enable detailed log.
- `excludeFields` specifies whether to exclude fields that are not required for the `equals` and `hashCode` methods. Optional,
  default is `Nil` (all `var` and `val` fields **exclude `protected [this]` and `private [this]`** in the class will be used to generate the two methods).
- Both `equals` and `hashCode` methods are affected by super classes, and `canEqual` uses `isInstanceOf` in `equals` method.
  Some equals implementations use `that.getClass == this.getClass`
- It uses simple hashcode algorithm, and the hashcode of the parent class are accumulated directly. The algorithm is also used by `case class`.
- If the class of the annotation has already defined the `canEqual` method with the same signature, `canEqual` will not be generated.
- Include the internal fields defined within a class, which named internal fields or member fields here.

**Example**

```scala
@equalsAndHashCode(verbose = true)
class Person(var name: String, var age: Int)
```

**Macro expansion code**

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