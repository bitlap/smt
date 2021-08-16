## @jacksonEnum

The `@jacksonEnum` annotation is used to provide `Jackson` serialization support for all Scala enumeration type parameters in the primary constructor of the class. (jackson and jackson-scala-module dependency needs to be introduced)

**Note**

- `verbose` Whether to enable detailed log. default is `false`.
- `nonTypeRefers` Specifies the enumeration type of the `TypeReference` subclass of `Jackson` that does not need to be created. default is `Nil`.
- Support `case class` and `class`.
- If the enumeration type has subclasses of `TypeReference`, no new subclasses will be generated,
  and `JsonScalaEnumeration` annotation will not be added to the parameters repeatedly. This is mainly used to solve conflict problems.

**Example**

```scala
@jacksonEnum(nonTypeRefers = Seq("EnumType")) 
class B(
        var enum1: EnumType.EnumType,
        enum2: EnumType2.EnumType2 = EnumType2.A,
        i: Int)
```

**Macro expansion code**

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