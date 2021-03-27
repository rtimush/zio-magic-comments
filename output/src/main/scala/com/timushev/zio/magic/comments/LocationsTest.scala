package com.timushev.zio.magic.comments

import _root_.zio.magic._
import zio._
import zio.clock._

class LocationsTest {

  val zio: ZIO[Clock, Nothing, Unit] = ZIO.never

  // Clock.live
  zio.inject(Clock.live)

  // Clock.live
  val sameLine: UIO[Unit] = zio.inject(Clock.live)

  // Clock.live
  val nextLine: UIO[Unit] =
    zio.inject(Clock.live)

  locally {
    // Clock.live
    val inBlock: UIO[Unit] =
      zio.inject(Clock.live)
  }

  def f(): Unit = {
    // Clock.live
    val inFunction: UIO[Unit] =
      zio.inject(Clock.live)
  }
}
