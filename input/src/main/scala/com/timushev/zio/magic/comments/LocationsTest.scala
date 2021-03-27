/*
rule = ZioMagicComments
 */
package com.timushev.zio.magic.comments

import _root_.zio.magic._
import zio._
import zio.clock._

class LocationsTest {

  val zio: ZIO[Clock, Nothing, Unit] = ZIO.never

  zio.inject(Clock.live)

  val sameLine: UIO[Unit] = zio.inject(Clock.live)

  val nextLine: UIO[Unit] =
    zio.inject(Clock.live)

  locally {
    val inBlock: UIO[Unit] =
      zio.inject(Clock.live)
  }

  def f(): Unit = {
    val inFunction: UIO[Unit] =
      zio.inject(Clock.live)
  }
}
