package com.timushev.zio.magic.comments

import zio.{Has, URIO, ZIO, ZLayer}
import zio.blocking.Blocking
import zio.clock.Clock
import _root_.zio.magic._

class ZioMagicCommentsTest {

  object Cake {
    trait Service
    val live: ZLayer[Clock with Blocking, Nothing, Cake] = ???
  }
  type Cake = Has[Cake.Service]

  val program: URIO[Cake, Unit] = ???

  //         Cake.live
  //      ┌──────┴─────┐
  // Clock.live  Blocking.live
  val magically: ZIO[Any, Nothing, Unit] =
    program.provideMagicLayer(Cake.live, Clock.live, Blocking.live)

}
