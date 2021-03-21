/*
rule = ZioMagicComments
 */
package com.timushev.zio.magic.comments

import _root_.zio.magic._
import zio._
import zio.clock._
import zio.test._

class MethodsTest {

  val zio: ZIO[NeedsClock, Nothing, Unit] = ZIO.never
  val zspec: ZSpec[NeedsClock, Nothing]   = Spec.test("", ZIO.never, TestAnnotationMap.empty)

  zio.provideMagicLayer(Clock.live, NeedsClock.layer)

  zio.provideSomeMagicLayer[Clock](NeedsClock.layer)

  zio.provideCustomMagicLayer(NeedsClock.layer)

  zspec.provideMagicLayer(Clock.live, NeedsClock.layer)

  zspec.provideMagicLayerShared(Clock.live, NeedsClock.layer)

  zspec.provideSomeMagicLayer[Clock](NeedsClock.layer)

  zspec.provideSomeMagicLayerShared[Clock](NeedsClock.layer)

  zspec.provideCustomMagicLayer(NeedsClock.layer)

  zspec.provideCustomMagicLayerShared(NeedsClock.layer)

  ZLayer.fromMagic[NeedsClock](Clock.live, NeedsClock.layer)

  ZLayer.fromSomeMagic[Clock, NeedsClock](NeedsClock.layer)

}
