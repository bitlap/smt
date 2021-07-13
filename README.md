<img align="right" width="30%" height="30%" src="img.png" alt="https://dreamylost.cn"/> 

# scala-macro-tools 

[![Build](https://github.com/jxnu-liguobin/scala-macro-tools/actions/workflows/ScalaCI.yml/badge.svg)](https://github.com/jxnu-liguobin/scala-macro-tools/actions/workflows/ScalaCI.yml) [![Maven Central](https://img.shields.io/maven-central/v/io.github.jxnu-liguobin/scala-macro-tools_2.13.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.jxnu-liguobin%22%20AND%20a:%22scala-macro-tools_2.13%22)
[![Version](https://img.shields.io/jetbrains/plugin/v/17202-scala-macro-tools)](https://plugins.jetbrains.com/plugin/17202-scala-macro-tools)

该库的目的
--

学习Scala宏编程（macro）和抽象语法树（ast）。

> 本项目目前处于实验阶段，有建议、意见或者问题欢迎提issue。如果本项目对你有帮助，欢迎点个star。

**[中文说明](./README.md) | [English](./README_EN.md)**

# 我能学到什么

- Scala2 宏编程
  - 了解Scala反射
  - 熟悉插值语法的应用，了解Scala Spec
  - 了解Scala AST相关的基本使用 
  - 知道如何编写宏注解（宏拓展）
  - 知道如何使用宏生成对象，类，方法，字段  
- 其他    
  - 知道劝退人的SBT如何优雅配置，诸如发布到云仓库，多版本构建，多模块项目
  - 了解如何利用Intellij-Scala插件编写自己的Scala插件
  - 了解Intellij是怎样支持语法提示的，插件工作流程
  - 知道如何写好scalatest单元测试
  - 类似的其他思考
    
# 环境

- 使用 Java 8, 11 编译通过
- 使用 Scala 2.11.x ~ 2.13.x 编译通过

# 功能

- `@toString`
- `@json`
- `@builder`
- `@synchronized`
- `@log`
- `@apply`
- `@constructor`

> 涉及到交互操作的注解在IDEA插件中都得到了支持。在插件市场中搜索`Scala-Macro-Tools`可下载。

[各个注解的说明](./docs/howToUse.md)

# 如何使用

添加库依赖，在sbt中

> 在gradle，maven中，通常`scala-macro-tools`被替换为`scala-macro-tools_2.12`这种。其中，`2.12`表示Scala版本号。

```scala
"io.github.jxnu-liguobin" %% "scala-macro-tools" % "<VERSION>"
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
