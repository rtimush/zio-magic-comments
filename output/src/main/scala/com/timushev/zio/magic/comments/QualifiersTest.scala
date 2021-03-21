package com.timushev.zio.magic.comments

import _root_.zio.magic._
import zio._
import zio.clock._
import zio.test._

class QualifiersTest {

  val urio: URIO[Clock, Unit] = ZIO.never
  // Clock.live
  urio.provideMagicLayer(Clock.live)

  val rio: RIO[Clock, Unit] = ZIO.never
  // Clock.live
  rio.provideMagicLayer(Clock.live)

  val zio: ZIO[Clock, Nothing, Unit] = ZIO.never
  // Clock.live
  zio.provideMagicLayer(Clock.live)

  val zspec: ZSpec[Clock, Nothing] = Spec.test("", ZIO.never, TestAnnotationMap.empty)
  // Clock.live
  zspec.provideMagicLayer(Clock.live)

  val spec: Spec[Clock, Nothing, Unit] = Spec.test("", ZIO.never, TestAnnotationMap.empty)
  // Clock.live
  spec.provideMagicLayer(Clock.live)

}
