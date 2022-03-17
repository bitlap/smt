<img align="right" width="30%" height="30%" src="img.png" alt="https://bitlap.org"/> 

# scala-macro-tools

[![Build](https://github.com/bitlap/scala-macro-tools/actions/workflows/ScalaCI.yml/badge.svg)](https://github.com/bitlap/scala-macro-tools/actions/workflows/ScalaCI.yml)
[![codecov](https://codecov.io/gh/bitlap/scala-macro-tools/branch/master/graph/badge.svg?token=IA596YRTOT)](https://codecov.io/gh/bitlap/scala-macro-tools)
[![Maven Central deprecated](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.13.svg?label=Maven%20Central$20deprecated)](https://search.maven.org/search?q=g:%22io.github.jxnu-liguobin%22%20AND%20a:%22scala-macro-tools_2.13%22)
[![Maven Central](https://img.shields.io/maven-central/v/org.bitlap/scala-macro-tools_2.13.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22org.bitlap%22%20AND%20a:%22scala-macro-tools_2.13%22)
[![Version](https://img.shields.io/jetbrains/plugin/v/17202-scala-macro-tools)](https://plugins.jetbrains.com/plugin/17202-scala-macro-tools)

该库的目的
--

学习Scala宏编程（macro）和抽象语法树（ast）。

> 本项目目前处于实验阶段，有建议、意见或者问题欢迎提issue。如果本项目对你有帮助，欢迎点个star。

**[中文说明](./README_CN.md) | [English](./README.md)**

# 环境

- Java 8、11 编译通过
- Scala 2.11.12、2.12.14、2.13.6 编译通过

# 功能

## core 模块

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

# 文档

[https://bitlap.org/zh-CN/lab/smt](https://bitlap.org/zh-CN/lab/smt)

# 如何使用

添加库依赖，在sbt中

> 在gradle，maven中，通常`scala-macro-tools`被替换为`scala-macro-tools_2.12`这种。其中，`2.12`表示Scala版本号。

```scala
"org.bitlap.tools" %% "scala-macro-tools" % "<VERSION>"
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
