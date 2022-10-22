# smt

| Project Stage | CI              | Codecov                                   |
|---------------|-----------------|-------------------------------------------|
| ![Stage]      | ![CI][Badge-CI] | [![codecov][Badge-Codecov]][Link-Codecov] |

| Scaladex                                                      | Jetbrains Plugin                              | Nexus Snapshots                                                  |
|---------------------------------------------------------------|-----------------------------------------------|------------------------------------------------------------------|
| [![smt Scala version support][Badge-Scaladex]][Link-Scaladex] | [![Version][Badge-Jetbrains]][Link-Jetbrains] | [![Sonatype Nexus (Snapshots)][Badge-Snapshots]][Link-Snapshots] |

# 环境

- Java 8+
- Scala 2.11.12、2.12.16、2.13.8

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
- JDBC `ResultSet`对象转样例类。

```scala
"org.bitlap" %% "smt-common" % "<VERSION>"
```

## csv

- CSV/TSV文件解析器（零依赖，类型安全）。

```scala
"org.bitlap" %% "smt-csv" % "<VERSION>" 
```

## csv-derive

- 为Scala`case class`自动派生`Converter`实例。

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

该库已发布到maven中央仓库，请使用最新版本。仅将本库导入构建系统（例如gradle、sbt）是不够的。你需要多走一步。

| Scala 2.11           | Scala 2.12           | Scala 2.13                     |
|----------------------|----------------------|--------------------------------|
| 导入 macro paradise 插件 | 导入 macro paradise 插件 | 开启 编译器标记 `-Ymacro-annotations` |

```scala
addCompilerPlugin("org.scalamacros" % "paradise_<your-scala-version>" % "<plugin-version>")
```

`<your-scala-version>`必须是Scala版本号的完整编号，如`2.12.13`，而不是`2.12`。

如果这不起作用，可以谷歌寻找替代品。

在`scala 2.13.x`版本中，macro paradise的功能直接包含在scala编译器中。然而，仍然必须启用编译器标志`-Ymacro annotations`。

# 特别感谢

<img src="https://resources.jetbrains.com/storage/products/company/brand/logos/IntelliJ_IDEA.svg" alt="IntelliJ IDEA logo.">

This project is developed using JetBrains IDEA.
Thanks to JetBrains for providing me with a free license, which is a strong support for me.

[Stage]: https://img.shields.io/badge/Project%20Stage-Development-yellowgreen.svg
[Badge-CI]: https://github.com/bitlap/smt/actions/workflows/ScalaCI.yml/badge.svg
[Badge-Scaladex]: https://index.scala-lang.org/bitlap/smt/smt-annotations/latest.svg?platform=jvm
[Badge-Jetbrains]: https://img.shields.io/jetbrains/plugin/v/17202-scala-macro-tools
[Badge-Codecov]: https://codecov.io/gh/bitlap/smt/branch/master/graph/badge.svg?token=IA596YRTOT
[Badge-Snapshots]: https://img.shields.io/nexus/s/org.bitlap/smt-annotations_2.13?server=https%3A%2F%2Fs01.oss.sonatype.org

[Link-Jetbrains]: https://plugins.jetbrains.com/plugin/17202-scala-macro-tools
[Link-Codecov]: https://codecov.io/gh/bitlap/smt
[Link-Scaladex]: https://index.scala-lang.org/bitlap/smt/smt-annotations
[Link-Snapshots]: https://s01.oss.sonatype.org/content/repositories/snapshots/org/bitlap/
