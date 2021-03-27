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

  zio.inject(Clock.live, NeedsClock.layer)

  zio.injectSome[Clock](NeedsClock.layer)

  zio.injectCustom(NeedsClock.layer)

  zspec.inject(Clock.live, NeedsClock.layer)

  zspec.injectShared(Clock.live, NeedsClock.layer)

  zspec.injectSome[Clock](NeedsClock.layer)

  // broken upstream
  // zspec.injectSomeShared[Clock](NeedsClock.layer)

  zspec.injectCustom(NeedsClock.layer)

  zspec.injectCustomShared(NeedsClock.layer)

  ZLayer.wire[NeedsClock](Clock.live, NeedsClock.layer)

  ZLayer.wireSome[Clock, NeedsClock](NeedsClock.layer)

}
