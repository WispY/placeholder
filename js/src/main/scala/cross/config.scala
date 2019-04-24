package cross

import cross.vec._

import scala.concurrent.duration._

object config {
  /** Describes the screen size where cards should be displayed at maximum size */
  val BaseScreenSize: Vec2i = 256 xy 256
  /** Describes the minimum UI scale */
  val MinScale: Int = 1
  /** Describes the maximum UI scale */
  val MaxScale: Int = 5

  val FlowerClusterSpread: Int = 5
  val FlowerClusterMinCount: Int = 2
  val FlowerClusterMaxCount: Int = 4
  val FlowerAnimationDelay: Int = 5

  /** Describes the basic animation speed */
  val AnimationDelay: FiniteDuration = 300.millis

  object log {
    /** Enables debug logs */
    val Debug = true
    /** Enables info logs */
    val Info = true
    /** Enables warnings logs */
    val Warnings = true
    /** Enables errors logs */
    val Errors = true
    /** Enables wire logs */
    val Wire = true
  }

}