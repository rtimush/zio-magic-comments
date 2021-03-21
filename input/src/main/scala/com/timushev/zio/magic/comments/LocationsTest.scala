/*
rule = ZioMagicComments
 */
package com.timushev.zio.magic.comments

import _root_.zio.magic._
import zio._
import zio.clock._

class LocationsTest {

  val zio: ZIO[Clock, Nothing, Unit] = ZIO.never

  zio.provideMagicLayer(Clock.live)

  val sameLine: UIO[Unit] = zio.provideMagicLayer(Clock.live)

  val nextLine: UIO[Unit] =
    zio.provideMagicLayer(Clock.live)

  locally {
    val inBlock: UIO[Unit] =
      zio.provideMagicLayer(Clock.live)
  }

  def f(): Unit = {
    val inFunction: UIO[Unit] =
      zio.provideMagicLayer(Clock.live)
  }
}
