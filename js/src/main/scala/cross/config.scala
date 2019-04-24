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

  /** How likely the parent branch will split into three */
  val TreeTripleBranchChance: Double = 0.3
  /** Delay between frames of tree spawn animation */
  val TreeSpawnAnimationDelay: Int = 3

  /** How far flowers can go apart in the cluster */
  val FlowerClusterSpread: Int = 5
  /** Minimum number of flowers in the cluster */
  val FlowerClusterMinCount: Int = 3
  /** Maximum number of flowers in the cluster */
  val FlowerClusterMaxCount: Int = 4
  /** Maximum delay of flowers opening within the cluster */
  val FlowerAnimationDelay: Int = 30
  /** Distance from mouse to flower cluster that will trigger it's animation */
  val FlowerOpenDistance: Int = 8

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