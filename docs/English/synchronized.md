## @synchronized

The `@synchronized` annotation is a more convenient and flexible synchronous annotation.

**Note**

- `lockedName` The name of the custom lock obj, default is `this`.
- Support static and instance methods.

**Example**

```scala

private final val obj = new Object

@synchronized(lockedName = "obj") // The default is this. If you fill in a non existent field name, the compilation will fail.
def getStr3(k: Int): String = {
  k + ""
}

// or
@synchronized //use this
def getStr(k: Int): String = {
  k + ""
}
```

**Macro expansion code**

```scala
// Note that it will not judge whether synchronized already exists, so if synchronized already exists, it will be used twice. 
// For example `def getStr(k: Int): String = this.synchronized(this.synchronized(k.$plus("")))
// It is not sure whether it will be optimized at the bytecode level.
def getStr(k: Int): String = this.synchronized(k.$plus(""))
```