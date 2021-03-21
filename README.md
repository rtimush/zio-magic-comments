zio-magic-comments
==================

`ZioMagicComments` is a [Scalafix](https://scalacenter.github.io/scalafix) rule that adds explanatory graphs
as comments to [zio-magic](https://github.com/kitlangton/zio-magic/) methods:

```scala
class Demo {
  val program: URIO[Cake, Unit] = ...

  //         Cake.live         ⎫
  //      ┌──────┴─────┐       ⎬ added by ZioMagicComments
  // Clock.live  Blocking.live ⎭
  val magically =
    program.provideMagicLayer(
      Cake.live,
      Clock.live,
      Blocking.live
    )
}
```

## Getting started 

Follow [the Scalafix documentation](https://scalacenter.github.io/scalafix/docs/users/installation.html) to make it possible to run Scalafix rules in your build.
Then either run
```sh
sbt> scalafix dependency:ZioMagicComments@com.timushev:zio-magic-comments:0.1.0
```
or include the rule in your `build.sbt`:
```scala
ThisBuild / scalafixDependencies += "com.timushev" %% "zio-magic-comments" % "0.1.0"
```
