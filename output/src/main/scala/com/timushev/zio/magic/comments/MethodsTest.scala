package com.timushev.zio.magic.comments

import _root_.zio.magic._
import zio._
import zio.clock._
import zio.test._

class MethodsTest {

  val zio: ZIO[NeedsClock, Nothing, Unit] = ZIO.never
  val zspec: ZSpec[NeedsClock, Nothing]   = Spec.test("", ZIO.never, TestAnnotationMap.empty)

  // NeedsClock.layer
  //       │
  //  Clock.live
  zio.inject(Clock.live, NeedsClock.layer)

  //    NeedsClock.layer
  //            │
  // ZLayer.requires[Clock]
  zio.injectSome[Clock](NeedsClock.layer)

  //    NeedsClock.layer
  //           │
  // ZLayer.requires[ZEnv]
  zio.injectCustom(NeedsClock.layer)

  // NeedsClock.layer
  //       │
  //  Clock.live
  zspec.inject(Clock.live, NeedsClock.layer)

  // NeedsClock.layer
  //       │
  //  Clock.live
  zspec.injectShared(Clock.live, NeedsClock.layer)

  //    NeedsClock.layer
  //            │
  // ZLayer.requires[Clock]
  zspec.injectSome[Clock](NeedsClock.layer)

  //    NeedsClock.layer
  //           │
  // ZLayer.requires[ZEnv]
  zspec.injectCustom(NeedsClock.layer)

  //    NeedsClock.layer
  //           │
  // ZLayer.requires[ZEnv]
  zspec.injectCustomShared(NeedsClock.layer)

  // NeedsClock.layer
  //       │
  //  Clock.live
  ZLayer.wire[NeedsClock](Clock.live, NeedsClock.layer)

  //    NeedsClock.layer
  //            │
  // ZLayer.requires[Clock]
  ZLayer.wireSome[Clock, NeedsClock](NeedsClock.layer)

}
