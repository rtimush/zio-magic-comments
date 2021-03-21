/* */
package com.timushev.zio.magic

import zio.clock.Clock
import zio._

package object comments {

  type NeedsClock = Has[NeedsClock.Service]
  object NeedsClock {
    trait Service
    val layer: ZLayer[Clock, Nothing, NeedsClock] = ZIO.never.toLayer
  }

}
