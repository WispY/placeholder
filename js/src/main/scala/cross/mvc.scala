package cross

import cross.asset.tree._
import cross.config._
import cross.data._
import cross.global.GlobalContext
import cross.logging.Logging
import cross.random._
import cross.timer.Timer
import cross.vec._

import scala.concurrent.Future
import scala.util.Random

object mvc extends GlobalContext with Logging {

  /** Contains application internal state */
  case class Model(tick: Writable[Long] = Data(0),
                   screen: Writable[Vec2i] = Data(0 xy 0),
                   scale: Writable[Double] = Data(1.0),
                   stage: Writable[Stages.Value] = Data(Stages.Loading),

                   loaded: Writable[Boolean] = Data(false),
                   tree: Writable[Option[TreeNode]] = Data(None))

  object Stages extends Enumeration {
    val Loading, Game = Value
  }

  object TreeVariations extends Enumeration {
    val Straight, Left, Right = Value
  }

  case class Asset(path: String)

  case class TreeAsset(asset: Asset,
                       level: Int,
                       rotation: Int,
                       variation: TreeVariations.Value,
                       rootAnchor: Vec2i,
                       branchAnchor: Vec2i,
                       flowerAnchors: List[Vec2i])

  case class TreeNode(asset: TreeAsset,
                      branches: List[TreeNode] = Nil)

  class Controller(val model: Model) {
    val timer = new Timer()

    /** Initializes the controller */
    def start(): Future[Unit] = Future {
      log.info("[controller] starting...")
      timer.start(60, () => model.tick.write(model.tick() + 1))
      bind()
      log.info("[controller] started")
    }

    /** Binds all internal calculations */
    private def bind(): Unit = {
      model.screen /> { case size =>
        val scale = ((size / BaseScreenSize).min min MaxScale) max MinScale
        log.info(s"size [$size] basic [${(size / BaseScreenSize).min}] scale [$scale]")
        model.scale.write(scale.toInt)
      }
    }

    /** Updates the rendering screen size */
    def setScreenSize(size: Vec2i): Unit = model.screen.write(size)

    /** Indicates that the assets were successfully loaded */
    def markLoaded(): Unit = model.loaded.write(true)

    /** Requests to go to Game stage */
    def jumpToGame(): Unit = model.stage.write(Stages.Game)

    def respawnTree(): Unit = model.tree.write(Some(randomTree))

    private def randomTree: TreeNode = {
      val random = new Random()

      def randomBranch(level: Int, rotation: Int): TreeAsset = random.oneOf(branches.filter(b => b.level == level && b.rotation == rotation))

      TreeNode(
        asset = randomBranch(0, 0),
        branches = List(
          TreeNode(
            asset = randomBranch(1, -1),
            branches = List(
              TreeNode(
                asset = randomBranch(2, -2),
                branches = List(
                  TreeNode(randomBranch(3, -3)),
                  TreeNode(randomBranch(3, -1))
                )
              ),
              TreeNode(
                asset = randomBranch(2, 0),
                branches = List(
                  TreeNode(randomBranch(3, -1)),
                  TreeNode(randomBranch(3, +1))
                )
              )
            )
          ),
          TreeNode(
            asset = randomBranch(1, +1),
            branches = List(
              TreeNode(
                asset = randomBranch(2, 0),
                branches = List(
                  TreeNode(randomBranch(3, -1)),
                  TreeNode(randomBranch(3, +1))
                )
              ),
              TreeNode(
                asset = randomBranch(2, +2),
                branches = List(
                  TreeNode(randomBranch(3, +1)),
                  TreeNode(randomBranch(3, +3))
                )
              )
            )
          )
        )
      )
    }
  }

}