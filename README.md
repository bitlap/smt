# scala-macro-tools

scala macro and abstract syntax tree learning code.

# @toString

- NOTE
    - Automatically ignore when use on `case` class.
    - Contains constructor parameters which have `val`/`var` modifier and class internal fields.
    - The existing custom `toString` method will fail to compile.

- source code
```scala
    @toString
    class TestClass(val i: Int = 0, var j: Int) {
      val y: Int = 0
      var z: String = "hello"
      var x: String = "world"
    }
```
- result of scalac
```scala
class TestClass extends scala.AnyRef {
  <paramaccessor> val i: Int = _;
  <paramaccessor> var j: Int = _;
  def <init>(i: Int = 0, j: Int) = {
    super.<init>();
    ()
  };
  val y: Int = 0;
  var z: String = "hello";
  var x: String = "world";
  override def toString(): String = scala.collection.immutable.List(i, j, y, z, x).toString.replace("List", "TestClass") // Use crude methods.
}
```

