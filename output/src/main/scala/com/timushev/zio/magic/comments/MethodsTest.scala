package com.timushev.zio.magic.comments

import _root_.zio.magic._
import zio._
import zio.clock._
import zio.test._

class MethodsTest {

  val zio: ZIO[NeedsClock, Nothing, Unit] = ZIO.never
  val zspec: ZSpec[NeedsClock, Nothing]   = Spec.test("", ZIO.never, TestAnnotationMap.empty)

  // NeedsClock.layer
  //      │
  // Clock.live
  zio.provideMagicLayer(Clock.live, NeedsClock.layer)

  //    NeedsClock.layer
  //            │
  // ZLayer.requires[Clock]
  zio.provideSomeMagicLayer[Clock](NeedsClock.layer)

  //    NeedsClock.layer
  //           │
  // ZLayer.requires[ZEnv]
  zio.provideCustomMagicLayer(NeedsClock.layer)

  // NeedsClock.layer
  //      │
  // Clock.live
  zspec.provideMagicLayer(Clock.live, NeedsClock.layer)

  // NeedsClock.layer
  //      │
  // Clock.live
  zspec.provideMagicLayerShared(Clock.live, NeedsClock.layer)

  //    NeedsClock.layer
  //            │
  // ZLayer.requires[Clock]
  zspec.provideSomeMagicLayer[Clock](NeedsClock.layer)

  //    NeedsClock.layer
  //            │
  // ZLayer.requires[Clock]
  zspec.provideSomeMagicLayerShared[Clock](NeedsClock.layer)

  //    NeedsClock.layer
  //           │
  // ZLayer.requires[ZEnv]
  zspec.provideCustomMagicLayer(NeedsClock.layer)

  //    NeedsClock.layer
  //           │
  // ZLayer.requires[ZEnv]
  zspec.provideCustomMagicLayerShared(NeedsClock.layer)

  // NeedsClock.layer
  //      │
  // Clock.live
  ZLayer.fromMagic[NeedsClock](Clock.live, NeedsClock.layer)

  //    NeedsClock.layer
  //            │
  // ZLayer.requires[Clock]
  ZLayer.fromSomeMagic[Clock, NeedsClock](NeedsClock.layer)

}
