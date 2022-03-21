<img align="right" width="20%" height="30%" src="img.png" alt="https://bitlap.org"/>

# scala-macro-tools

| CI                                                                                                                                                                         | Codecov                                                                                                                                                     |
| -------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [![Build](https://github.com/bitlap/scala-macro-tools/actions/workflows/ScalaCI.yml/badge.svg)](https://github.com/bitlap/scala-macro-tools/actions/workflows/ScalaCI.yml) | [![codecov](https://codecov.io/gh/bitlap/scala-macro-tools/branch/master/graph/badge.svg?token=IA596YRTOT)](https://codecov.io/gh/bitlap/scala-macro-tools) |

| Scaladex                                                                                                                                                                                                                                | Maven Central                                                                                                                                               | Jetbrains Plugin                                                                                                                              |
|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------| ----------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------- |
| [![scala-macro-tools Scala version support](https://index.scala-lang.org/bitlap/scala-macro-tools/scala-macro-tools/latest-by-scala-version.svg?platform=jvm)](https://index.scala-lang.org/bitlap/scala-macro-tools/scala-macro-tools) | [![Maven Central](https://img.shields.io/maven-central/v/org.bitlap/scala-macro-tools_2.13.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22org.bitlap%22%20AND%20a:%22scala-macro-tools_2.13%22) | [![Version](https://img.shields.io/jetbrains/plugin/v/17202-scala-macro-tools)](https://plugins.jetbrains.com/plugin/17202-scala-macro-tools) |

Motivation
--

Learn Scala macro and abstract syntax tree.

> The project is currently experimental

[中文说明](./README_CN.md) | [English](./README.md)

# Environment

- Compile passed in Java 8、11
- Compile passed in Scala 2.11.12、2.12.14、2.13.6

# Module Features

## `tools`

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

> The intellij plugin named `Scala-Macro-Tools` in marketplace.

## `cacheable-core`

A cache like Spring `@Cacheable` and `@cacheEvict` based on zio. It has no implementation of storage media. 

- `@cacheable` / `Cache.apply`
- `@cacheEvict` / `Cache.evict`

## `cacheable-caffeine`

A memory cache based on zio and caffeine. It needs `cacheable-core` module.

## `cacheable-redis`

A distributed cache based on zio and zio-redis. It needs `cacheable-core` module.

# Document

[https://bitlap.org/lab/smt](https://bitlap.org/lab/smt)

# How to use

Add library dependency

```scala
"org.bitlap" %% "smt-tools" % "<VERSION>" // named smt-tools since 0.4.0 

// when you need cacheable module (not support Scala 2.11.x)
// include dependencies: zio,zio-streams,zio-logging
"org.bitlap" %% "smt-cacheable-core" % "<VERSION>" 
// local cache, include dependencies: config, caffeine
"org.bitlap" %% "smt-cacheable-caffeine" % "<VERSION>" 
// distributed cache, include dependencies: zio-redis,config,zio-schema, optional (zio-schema-protobuf,zio-schema-derivation for case class)
"org.bitlap" %% "smt-cacheable-redis" % "<VERSION>"
```

The artefacts have been uploaded to Maven Central. Importing the library into your build system (e.g gradle, sbt), is not enough. You need to follow an extra step.

| Scala 2.11                   | Scala 2.12                   | Scala 2.13                                          |
| ---------------------------- | ---------------------------- | --------------------------------------------------- |
| Import macro paradise plugin | Import macro paradise plugin | Enable compiler flag `-Ymacro-annotations` required |

```scala
addCompilerPlugin("org.scalamacros" % "paradise_<your-scala-version>" % "<plugin-version>")
```

Where `<your-scala-version>` must be the full scala version. For example 2.12.13, and not 2.12.

If that doesn't work, google for alternatives.

In version scala`2.13.x`, the functionality of macro paradise has been included in the scala compiler directly. However,
you must still enable the compiler flag `-Ymacro-annotations`.
