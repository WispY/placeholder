package cross

import cross.asset.tree._
import cross.common._
import cross.config._
import cross.global.GlobalContext
import cross.logging.Logging
import cross.timer.Timer

import scala.concurrent.Future
import scala.util.Random

object mvc extends GlobalContext with Logging {

  /** Contains application internal state */
  case class Model(tick: Writeable[Long] = Data(0),
                   screen: Writeable[Vec2i] = Data(0 xy 0),
                   scale: Writeable[Double] = Data(1.0),
                   stage: Writeable[Stages.Value] = Data(Stages.Loading),
                   mouse: Writeable[Vec2d] = Data(Vec2d.Zero),

                   loaded: Writeable[Boolean] = Data(false),
                   trees: Writeable[List[TreeNode]] = Data(Nil))

  object Stages extends Enumeration {
    val Loading, Game = Value
  }

  object TreeVariations extends Enumeration {
    val Straight, Left____, Right___ = Value
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
        model.scale.write(scale.toInt)
      }
    }

    /** Updates the rendering screen size */
    def setScreenSize(size: Vec2i): Unit = model.screen.write(size)

    /** Indicates that the assets were successfully loaded */
    def markLoaded(): Unit = model.loaded.write(true)

    /** Requests to go to Game stage */
    def jumpToGame(): Unit = model.stage.write(Stages.Game)

    /** Updates the global mouse position on the screen */
    def setMousePosition(mouse: Vec2d): Unit = model.mouse.write(mouse)

    /** Requests to spawn the new random tree */
    def respawnTree(): Unit = {
      val tree = randomTree
      model.trees.write(animatedTree(tree, tree :: Nil))
    }

    private val twoMapping = List(
      (+1, -1, TreeVariations.Left____) :: (+1, +0, TreeVariations.Straight) :: Nil,
      (+1, -1, TreeVariations.Left____) :: (+1, +0, TreeVariations.Right___) :: Nil,
      (+1, -1, TreeVariations.Straight) :: (+1, +0, TreeVariations.Right___) :: Nil,

      (+1, +0, TreeVariations.Left____) :: (+1, +1, TreeVariations.Straight) :: Nil,
      (+1, +0, TreeVariations.Left____) :: (+1, +1, TreeVariations.Right___) :: Nil,
      (+1, +0, TreeVariations.Straight) :: (+1, +1, TreeVariations.Right___) :: Nil,

      (+1, -1, TreeVariations.Left____) :: (+1, +1, TreeVariations.Straight) :: Nil,
      (+1, -1, TreeVariations.Left____) :: (+1, +1, TreeVariations.Right___) :: Nil,
      (+1, -1, TreeVariations.Straight) :: (+1, +1, TreeVariations.Right___) :: Nil,
      (+1, -1, TreeVariations.Straight) :: (+1, +1, TreeVariations.Straight) :: Nil,


      (+2, -1, TreeVariations.Left____) :: (+1, +0, TreeVariations.Straight) :: Nil,
      (+2, -1, TreeVariations.Left____) :: (+1, +0, TreeVariations.Right___) :: Nil,
      (+2, -1, TreeVariations.Straight) :: (+1, +0, TreeVariations.Right___) :: Nil,

      (+1, +0, TreeVariations.Left____) :: (+2, +1, TreeVariations.Straight) :: Nil,
      (+1, +0, TreeVariations.Left____) :: (+2, +1, TreeVariations.Right___) :: Nil,
      (+1, +0, TreeVariations.Straight) :: (+2, +1, TreeVariations.Right___) :: Nil,
    )

    private val threeMapping = List(
      (+1, -1, TreeVariations.Left____) :: (+1, +0, TreeVariations.Straight) :: (+1, +1, TreeVariations.Right___) :: Nil,
      (+1, -1, TreeVariations.Left____) :: (+1, +0, TreeVariations.Straight) :: (+2, +1, TreeVariations.Right___) :: Nil,
      (+2, -1, TreeVariations.Left____) :: (+1, +0, TreeVariations.Straight) :: (+1, +1, TreeVariations.Right___) :: Nil,
      (+2, -1, TreeVariations.Left____) :: (+1, +0, TreeVariations.Left____) :: (+1, +1, TreeVariations.Right___) :: Nil,
      (+2, -1, TreeVariations.Straight) :: (+1, +0, TreeVariations.Straight) :: (+1, +1, TreeVariations.Right___) :: Nil,
      (+1, -1, TreeVariations.Left____) :: (+1, +0, TreeVariations.Right___) :: (+2, +1, TreeVariations.Right___) :: Nil,
      (+1, -1, TreeVariations.Left____) :: (+1, +0, TreeVariations.Straight) :: (+2, +1, TreeVariations.Straight) :: Nil,
    )

    private def randomTree: TreeNode = {
      val random = new Random()
      val maxLevel = branches.map(b => b.level).max

      def rec(asset: TreeAsset): TreeNode = {
        val childCount = if (asset.level == maxLevel) 0 else if (random.nextDouble() < TreeTripleBranchChance) 3 else 2
        val mapping = childCount match {
          case 0 => Nil
          case 2 => twoMapping
          case 3 => threeMapping
        }
        val children = random
          .shuffle(mapping)
          .map { list =>
            list.flatMap { case (levelGain, rotationGain, variation) =>
              branches.find(b => b.level == asset.level + levelGain && b.rotation == asset.rotation + rotationGain && b.variation == variation)
            }
          }
          .find { assets => assets.size == childCount }
          .map(assets => assets.map(rec))
        TreeNode(asset, children.getOrElse(Nil))
      }

      rec(random.oneOf(branches.filter(b => b.level == 0)))
    }

    private def animatedTree(root: TreeNode, tail: List[TreeNode]): List[TreeNode] = {
      downgradeTree(root) match {
        case Some(frame) => animatedTree(frame, frame +: tail)
        case None => tail
      }
    }

    private def downgradeTree(root: TreeNode): Option[TreeNode] = {
      branches
        .find(b => b.level == (root.asset.level + 1) && b.rotation == root.asset.rotation && b.variation == root.asset.variation)
        .map(asset => TreeNode(asset, root.branches.flatMap(downgradeTree)))
    }
  }

}