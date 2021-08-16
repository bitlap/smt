## @jacksonEnum

`@jacksonEnum`注解用于为类的主构造函数中的所有Scala枚举类型的参数提供`Jackson`序列化的支持。（jackson和jackson-scala-module依赖需要自己引入）

**说明**
  
- `verbose` 指定是否开启详细编译日志。可选，默认`false`。
- `nonTypeRefers` 指定不需要创建`Jackson`的`TypeReference`子类的枚举类型。可选，默认`Nil`。
- 支持`case class`和`class`。
- 如果枚举类型存在`TypeReference`的子类，则不会生成新的子类，也不会重复添加`@JsonScalaEnumeration`注解到参数上。这主要用于解决冲突问题。

**示例**

```scala
@jacksonEnum(nonTypeRefers = Seq("EnumType")) 
class B(
        var enum1: EnumType.EnumType,
        enum2: EnumType2.EnumType2 = EnumType2.A,
        i: Int)
```

**宏生成的中间代码**

```scala
 class EnumType2TypeRefer extends _root_.com.fasterxml.jackson.core.`type`.TypeReference[EnumType2.type] {
    def <init>() = {
      super.<init>();
      ()
    }
  };
  class B extends scala.AnyRef {
    <paramaccessor> var enum1: JacksonEnumTest.this.EnumType.EnumType = _;
    @new com.fasterxml.jackson.module.scala.JsonScalaEnumeration(classOf[EnumType2TypeRefer]) <paramaccessor> private[this] val enum2: JacksonEnumTest.this.EnumType2.EnumType2 = _;
    <paramaccessor> private[this] val i: Int = _;
    def <init>(enum1: JacksonEnumTest.this.EnumType.EnumType, @new com.fasterxml.jackson.module.scala.JsonScalaEnumeration(classOf[EnumType2TypeRefer]) enum2: JacksonEnumTest.this.EnumType2.EnumType2 = EnumType2.A, i: Int) = {
      super.<init>();
      ()
    }
  };
  ()
```