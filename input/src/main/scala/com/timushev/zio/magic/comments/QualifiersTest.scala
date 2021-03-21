/*
rule = ZioMagicComments
 */
package com.timushev.zio.magic.comments

import _root_.zio.magic._
import zio._
import zio.clock._
import zio.test._

class QualifiersTest {

  val urio: URIO[Clock, Unit] = ZIO.never
  urio.provideMagicLayer(Clock.live)

  val rio: RIO[Clock, Unit] = ZIO.never
  rio.provideMagicLayer(Clock.live)

  val zio: ZIO[Clock, Nothing, Unit] = ZIO.never
  zio.provideMagicLayer(Clock.live)

  val zspec: ZSpec[Clock, Nothing] = Spec.test("", ZIO.never, TestAnnotationMap.empty)
  zspec.provideMagicLayer(Clock.live)

  val spec: Spec[Clock, Nothing, Unit] = Spec.test("", ZIO.never, TestAnnotationMap.empty)
  spec.provideMagicLayer(Clock.live)

}
