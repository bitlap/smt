<img align="right" width="20%" height="30%" src="img.png" alt="https://bitlap.org"/>

# scala-macro-tools

| Project Stage | CI              | Codecov                                   |
|---------------|-----------------|-------------------------------------------|
| ![Stage]      | ![CI][Badge-CI] | [![codecov][Badge-Codecov]][Link-Codecov] |

| Scaladex                                                                    | Jetbrains Plugin                              | Nexus Snapshots                                                  |
|-----------------------------------------------------------------------------|-----------------------------------------------|------------------------------------------------------------------|
| [![scala-macro-tools Scala version support][Badge-Scaladex]][Link-Scaladex] | [![Version][Badge-Jetbrains]][Link-Jetbrains] | [![Sonatype Nexus (Snapshots)][Badge-Snapshots]][Link-Snapshots] |

Motivation
--

Learn Scala macro and abstract syntax tree.

> The project is currently experimental

[中文说明](./README_CN.md) | [English](./README.md)

# Environment

- Compile passed in Java 8、11
- Compile passed in Scala 2.11.12、2.12.14、2.13.8

# Document

[https://bitlap.org/en-US/lab/smt](https://bitlap.org/en-US/lab/smt)

# How to use

## csv-core

- `Converter` A basic CSV converter
- `CsvableBuilder` Support for converting Scala`case class`to one CSV line in a custom way.
- `ScalableBuilder` Support for converting one CSV line to Scala`case class`in a custom way.
- Zero dependency, type-safe

```scala
"org.bitlap" %% "smt-csv-core" % "<VERSION>" // since 0.5.2
```

## csv-derive

- `DeriveCsvConverter` Automatically derive`Converter`instances for Scala`case class`

```scala
"org.bitlap" %% "smt-csv-derive" % "<VERSION>" // since 0.5.2
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

> The intellij plugin named `Scala-Macro-Tools` in marketplace.

```scala
"org.bitlap" %% "smt-tools" % "<VERSION>" // named smt-tools since 0.4.0 
```

## cacheable-core

A cache like Spring `@Cacheable` and `@cacheEvict` based on zio. It has no implementation of storage media.

- `@cacheable` / `Cache.apply`
- `@cacheEvict` / `Cache.evict`

```scala
// cache API, include dependencies: zio, zio-streams, zio-logging
"org.bitlap" %% "smt-cacheable-core" % "<VERSION>" // not support Scala2.11.x
```

## cacheable-redis

A distributed cache based on zio and zio-redis. It needs `cacheable-core` module.

> TODO Not unavailable, no distributed lock

```scala
// distributed cache, include dependencies: zio-redis, config, zio-schema, zio-schema-json, optional (zio-schema-derivation for case class)
// dependsOn `smt-cacheable-core`
"org.bitlap" %% "smt-cacheable-redis" % "<VERSION>" // not support Scala2.11.x
```

## cacheable-caffeine

A memory cache based on zio and caffeine. It needs `cacheable-core` module.

```scala
// local cache, include dependencies: config, caffeine
// dependsOn `smt-cacheable-core`
"org.bitlap" %% "smt-cacheable-caffeine" % "<VERSION>" // not support Scala2.11.x
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