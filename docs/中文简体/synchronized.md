## @synchronized

`@synchronized`注解是一个更方便、更灵活的用于同步方法的注解。

**说明**

- `lockedName` 指定自定义的锁对象的名称。可选，默认`this`。
- 支持静态方法（`object`中的函数）和实例方法（`class`中的函数）。

**示例**

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

**宏生成的中间代码**

```scala
// 注意，它不会判断synchronized是否已经存在，因此如果synchronized已经存在，它将被使用两次。如下 
// `def getStr(k: Int): String = this.synchronized(this.synchronized(k.$plus("")))
// 目前还不确定是否在字节码级别会被优化。
def getStr(k: Int): String = this.synchronized(k.$plus(""))
```