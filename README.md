# smt

| CI              | Codecov                                   |
|-----------------|-------------------------------------------|
| ![CI][Badge-CI] | [![codecov][Badge-Codecov]][Link-Codecov] |

| Scaladex                                                      | Jetbrains Plugin                              |
|---------------------------------------------------------------|-----------------------------------------------|
| [![smt Scala version support][Badge-Scaladex]][Link-Scaladex] | [![Version][Badge-Jetbrains]][Link-Jetbrains] |

# 环境

- Java 8+
- Scala 2.11.12、2.12.16、2.13.8 => Scala3版本 [bitlap/rolls](https://github.com/bitlap/rolls)

# 文档

[详细文档 https://bitlap.org/lab/smt](https://bitlap.org/lab/smt)

# 如何使用

添加库依赖，下面是如何在 SBT 中使用

> 在gradle，maven中，通常`smt-annotations`被替换为`smt-annotations_2.12`，其中，`2.12`表示Scala版本号。

## cache

- 统一缓存API，缓存适配器（零依赖，类型安全）。
```scala
"org.bitlap" %% "smt-cache" % "<VERSION>"
```

## common

- 通用的宏操作API的封装。
- 对象转换器（零依赖，类型安全）。
- JDBC `ResultSet` 转换器。

```scala
"org.bitlap" %% "smt-common" % "<VERSION>"
```

## csv

- CSV/TSV文件读写工具（零依赖，类型安全）。

```scala
"org.bitlap" %% "smt-csv" % "<VERSION>" 
```

## csv-derive

- 自动派生CSV/TSV文件读写工具。

```scala
"org.bitlap" %% "smt-csv-derive" % "<VERSION>" 
```

## annotations

- `@toString`
- `@builder`
- `@log`
- `@apply`
- `@constructor`
- `@equalsAndHashCode`
- `@elapsed`
- `@javaCompatible`

> Intellij插件 `Scala-Macro-Tools`。

```scala
"org.bitlap" %% "smt-annotations" % "<VERSION>" 
```

该库已发布到maven中央仓库，请使用最新版本。仅将本库导入构建系统（例如gradle、sbt）是不够的。还需要配置：

| Scala 2.11           | Scala 2.12           | Scala 2.13                     |
|----------------------|----------------------|--------------------------------|
| 导入 macro paradise 插件 | 导入 macro paradise 插件 | 开启 编译器标记 `-Ymacro-annotations` |

```scala
// 导入 macro paradise 插件
// <your-scala-version> 必须是Scala版本号的完整编号，如2.12.13，而不是2.12。
addCompilerPlugin("org.scalamacros" % "paradise_<your-scala-version>" % "<plugin-version>")
```

在`scala 2.13.x`版本中，需增加scalac参数`-Ymacro annotations`。

# 特别感谢

<img src="https://resources.jetbrains.com/storage/products/company/brand/logos/IntelliJ_IDEA.svg" alt="IntelliJ IDEA logo.">

This project is developed using JetBrains IDEA.
Thanks to JetBrains for providing me with a free license, which is a strong support for me.

[Badge-CI]: https://github.com/bitlap/smt/actions/workflows/ci.yml/badge.svg
[Badge-Scaladex]: https://index.scala-lang.org/bitlap/smt/smt-annotations/latest.svg?platform=jvm
[Badge-Jetbrains]: https://img.shields.io/jetbrains/plugin/v/17202-scala-macro-tools
[Badge-Codecov]: https://codecov.io/gh/bitlap/smt/branch/master/graph/badge.svg?token=IA596YRTOT

[Link-Jetbrains]: https://plugins.jetbrains.com/plugin/17202-scala-macro-tools
[Link-Codecov]: https://codecov.io/gh/bitlap/smt
[Link-Scaladex]: https://index.scala-lang.org/bitlap/smt/smt-annotations
