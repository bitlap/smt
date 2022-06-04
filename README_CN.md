<img align="right" width="20%" height="30%" src="img.png" alt="https://bitlap.org"/> 

# scala-macro-tools

| Project Stage | CI              | Codecov                                   |
|---------------|-----------------|-------------------------------------------|
| ![Stage]      | ![CI][Badge-CI] | [![codecov][Badge-Codecov]][Link-Codecov] |

| Scaladex                                                                    | Jetbrains Plugin                              | Nexus Snapshots                                                  |
|-----------------------------------------------------------------------------|-----------------------------------------------|------------------------------------------------------------------|
| [![scala-macro-tools Scala version support][Badge-Scaladex]][Link-Scaladex] | [![Version][Badge-Jetbrains]][Link-Jetbrains] | [![Sonatype Nexus (Snapshots)][Badge-Snapshots]][Link-Snapshots] |

该库的目的
--

学习Scala宏编程（macro）和抽象语法树（ast）。

> 本项目目前处于实验阶段，有建议、意见或者问题欢迎提issue。如果本项目对你有帮助，欢迎点个star。

**[中文说明](./README_CN.md) | [English](./README.md)**

# 环境

- Java 8、11 编译通过
- Scala 2.11.12、2.12.14、2.13.8 编译通过

# 文档

[详细文档 https://bitlap.org/lab/smt](https://bitlap.org/lab/smt)

# 如何使用

添加库依赖，下面是如何在 SBT 中使用

> 在gradle，maven中，通常`smt-tools`被替换为`smt-tools_2.12`这种。其中，`2.12`表示Scala版本号。

## csv-core

- `Converter` 基础的CSV转换器
- `CsvableBuilder` 支持以自定义的方式将Scala`case class`转化为一行CSV字符串
- `ScalableBuilder` 支持以自定义的方式将一行CSV字符串转化为Scala`case class`
- 零依赖，类型安全

```scala
"org.bitlap" %% "smt-csv-core" % "<VERSION>" // 从0.5.2开始 
```

## csv-derive

- `DeriveCsvConverter` 为Scala`case class`自动派生`Converter`实例

```scala
"org.bitlap" %% "smt-csv-derive" % "<VERSION>" // 从0.5.2开始 
```

## tools

- `@toString`
- `@json`
- `@builder`
- `@log`
- `@apply`
- `@constructor`
- `@equalsAndHashCode`
- `@jacksonEnum`
- `@elapsed`
- `@javaCompatible`
- `ProcessorCreator`

> Intellij插件 `Scala-Macro-Tools`。

```scala
"org.bitlap" %% "smt-tools" % "<VERSION>" //从0.4.0开始名字改成 smt-tools 
```

## cacheable-core

基于zio的类似Spring`@Cacheable`和`@CacheEvict`注解的缓存API定义。该模块不包含具体的存储媒介。

- `@cacheable` / `Cache.apply`
- `@cacheEvict` / `Cache.evict`

```scala
// 内部包含的依赖: zio, zio-streams, zio-logging
"org.bitlap" %% "smt-cacheable-core" % "<VERSION>" // 不支持Scala2.11.x
```

## cacheable-redis

基于zio和zio-redis的分布式缓存实现，内部依赖`cacheable-core`。

> TODO，目前不可用，无分布式锁

```scala
// 分布式缓存, 内部包含的依赖: zio-redis, config, zio-schema, zio-schema-json, 可选的 (zio-schema-derivation用于样例类序列化)
// 依赖于`smt-cacheable-core`
"org.bitlap" %% "smt-cacheable-redis" % "<VERSION>" // 不支持Scala2.11.x
```

## cacheable-caffeine

基于zio和caffeine的内存缓存实现，内部依赖`cacheable-core`。

```scala
// 本地缓存, 内部包含的依赖: config, caffeine
// 依赖于`smt-cacheable-core`
"org.bitlap" %% "smt-cacheable-caffeine" % "<VERSION>" // 不支持Scala2.11.x
```

该库已发布到maven中央仓库，请使用最新版本。仅将本库导入构建系统（例如gradle、sbt）是不够的。你需要多走一步。

| Scala 2.11               | Scala 2.12               | Scala 2.13                            |
| ------------------------ | ------------------------ | ------------------------------------- |
| 导入 macro paradise 插件 | 导入 macro paradise 插件 | 开启 编译器标记 `-Ymacro-annotations` |

```scala
addCompilerPlugin("org.scalamacros" % "paradise_<your-scala-version>" % "<plugin-version>")
```

`<your-scala-version>`必须是Scala版本号的完整编号，如`2.12.13`，而不是`2.12`。

如果这不起作用，可以谷歌寻找替代品。

在`scala 2.13.x`版本中，macro paradise的功能直接包含在scala编译器中。然而，仍然必须启用编译器标志`-Ymacro annotations`。

[Stage]: https://img.shields.io/badge/Project%20Stage-Experimental-yellow.svg
[Badge-CI]: https://github.com/bitlap/scala-macro-tools/actions/workflows/ScalaCI.yml/badge.svg
[Badge-Scaladex]: https://index.scala-lang.org/bitlap/scala-macro-tools/smt-tools/latest-by-scala-version.svg?platform=jvm
[Badge-Jetbrains]: https://img.shields.io/jetbrains/plugin/v/17202-scala-macro-tools
[Badge-Codecov]: https://codecov.io/gh/bitlap/scala-macro-tools/branch/master/graph/badge.svg?token=IA596YRTOT
[Badge-Snapshots]: https://img.shields.io/nexus/s/org.bitlap/smt-tools_2.13?server=https%3A%2F%2Fs01.oss.sonatype.org

[Link-Jetbrains]: https://plugins.jetbrains.com/plugin/17202-scala-macro-tools
[Link-Codecov]: https://codecov.io/gh/bitlap/scala-macro-tools
[Link-Scaladex]: https://index.scala-lang.org/bitlap/scala-macro-tools/smt-tools
[Link-Snapshots]: https://s01.oss.sonatype.org/content/repositories/snapshots/org/bitlap/