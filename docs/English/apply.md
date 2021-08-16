## @apply

The `@apply` annotation is used to generate `apply` method for primary construction of ordinary classes.

**Note**

- `verbose` Whether to enable detailed log.
- Only support `class`.
- Only support **primary construction**.

**Example**

```scala
@apply @toString class B2(int: Int, val j: Int, var k: Option[String] = None, t: Option[Long] = Some(1L))
println(B2(1, 2))
```