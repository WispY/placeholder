package cross.sakura.stage

import cross.common._
import cross.component.pixel.Button
import cross.component.{Component, Stage}
import cross.ops._
import cross.pixi._
import cross.sakura.asset.flower._
import cross.sakura.asset.ui._
import cross.sakura.config._
import cross.sakura.mvc.{Asset, Controller, TreeNode}
import cross.sakura.stage.GameStage.FlowerCluster
import cross.util.animation.Animation
import cross.util.global.GlobalContext
import cross.util.logging.Logging

import scala.concurrent.Future
import scala.util.Random

//noinspection TypeAnnotation
class GameStage()(implicit controller: Controller, app: Application) extends Stage with Logging with GlobalContext {
  override protected def logKey: String = "sakura/game"
  lazy val pixiContainer = new Container()
  lazy val pixiCenter = centerStage
  lazy val pixiSky = pixiCenter.sub
  lazy val pixiSpawnTree = Button(`asset-tree-normal`, `asset-tree-hover`, `asset-tree-pressed`, `asset-tree-normal`)
  lazy val pixiIslandBack = pixiCenter.sub
  lazy val pixiTree = pixiCenter.sub
  lazy val pixiIslandFront = pixiCenter.sub
  lazy val pixiFlowers = pixiCenter.sub
  lazy val pixiIsland = `asset-island-front-1`.sprite

  val treePosition = 0 xy 85
  val islandPosition = -64 xy (treePosition.y - 13)

  var animating = false
  var animationStart = 0L
  var treeBuffer: List[(Container, Container)] = Nil
  var currentTreeContainers: List[Container] = Nil
  var closedFlowers: List[FlowerCluster] = Nil
  var shadow = 0
  val islandAssets = List(`asset-island-front-1`, `asset-island-front-2`, `asset-island-front-3`, `asset-island-front-4`, `asset-island-front-5`)

  override lazy val create: Future[Unit] = Future {
    log.info("setting up...")
    pixiCenter.addTo(pixiContainer)
    pixiSky.positionAt(treePosition)
    `asset-sky`.sprite.anchorAtCenter.addTo(pixiSky)
    pixiIslandBack.positionAt(islandPosition)
    pixiTree.positionAt(treePosition)
    pixiIslandFront.positionAt(islandPosition)
    pixiFlowers.positionAt(treePosition)
    pixiSpawnTree
      .onClick { button =>
        button.setEnabled(false)
        controller.respawnTree()
      }
      .withPixi { pixi =>
        pixi
          .addTo(pixiCenter)
          .positionAt(0 xy 110)
      }

    `asset-island-back`.sprite.addTo(pixiIslandBack)
    pixiIsland.addTo(pixiIslandFront)

    controller.model.trees /> {
      case trees =>
        shadow = 0
        pixiIsland.texture = islandAssets.head.texture
        pixiTree.removeChildren
        pixiFlowers.removeChildren
        treeBuffer = trees.zipWithIndex.map { case (tree, index) =>
          val (tc, fc) = buildTree(tree, spawnFlowers = index == trees.size - 1)
          tc.visibleTo(false).addTo(pixiTree)
          fc.visibleTo(false).addTo(pixiFlowers)
          (tc, fc)
        }
        if (treeBuffer.nonEmpty) {
          animationStart = controller.model.tick.read
          animating = true
        } else {
          pixiSpawnTree.setEnabled(true)
        }
    }

    controller.model.tick /> { case tick if animating =>
      if (tick - animationStart >= TreeSpawnAnimationDelay) {
        shadow = shadow + 1
        pixiIsland.texture = islandAssets.lift(shadow).getOrElse(islandAssets.last).texture
        animationStart = tick
        val (tc, fc) = treeBuffer.head
        tc.visibleTo(true)
        fc.visibleTo(true)
        currentTreeContainers.foreach(c => c.detach)
        currentTreeContainers = tc :: fc :: Nil
        treeBuffer = treeBuffer.tail
        if (treeBuffer.isEmpty) {
          animating = false
          pixiSpawnTree.enable()
        }
      }
    }

    controller.model.mouse /> { case mouse if !animating =>
      closedFlowers = closedFlowers.filter { cluster =>
        if (mouse.near(cluster.getGlobalPosition(), FlowerOpenDistance * controller.model.scale.read)) {
          cluster.open()
          false
        } else {
          true
        }
      }
    }

    log.info("created")
  }

  def buildTree(tree: TreeNode, spawnFlowers: Boolean): (Container, Container) = {
    val random = new Random()
    val treeContainer = new Container()
    val flowerContainer = new Container()

    def rec(parent: Container, node: TreeNode, offset: Vec2i): Unit = {
      val position = node.asset.rootAnchor.flip + node.asset.branchAnchor
      val sub = parent.sub
        .positionAt(position)
      node.asset.asset.sprite
        .addTo(parent)
        .positionAt(node.asset.rootAnchor.flip)
      if (spawnFlowers) {
        node.asset.flowerAnchors.foreach { anchor =>
          closedFlowers = closedFlowers :+ new FlowerCluster(random.nextLong).withPixi { pixi =>
            val absolutePosition = offset + node.asset.rootAnchor.flip + anchor
            pixi.positionAt(absolutePosition).addTo(flowerContainer)
          }
        }
      }
      node.branches.foreach(b => rec(sub, b, offset + position))
    }

    rec(treeContainer, tree, 0 xy 0)
    (treeContainer, flowerContainer)
  }

  override def fadeIn(): Animation = pixiContainer.fadeIn

  override def fadeOut(): Animation = pixiContainer.fadeOut

  override val toPixi: DisplayObject = pixiContainer
}

//noinspection TypeAnnotation
object GameStage {

  class Flower(assets: List[Asset])(implicit controller: Controller, app: Application) extends Component {
    lazy val pixiContainer = new Container()
    lazy val pixiSprite = assets.head.sprite.addTo(pixiContainer).positionAt(-4 xy -4).visibleTo(false)
    var opened = false

    def open(delay: Int): Unit = if (!opened) {
      val start = controller.model.tick.read + delay
      controller.model.tick /> { case tick if !opened =>
        val offset = (tick - start).toInt / 3
        if (offset >= 0) {
          pixiSprite.visibleTo(true).texture = assets.lift(offset).getOrElse(assets.last).texture
          if (offset >= assets.size) opened = true
        }
      }
    }

    override def toPixi: DisplayObject = pixiContainer
  }

  class FlowerCluster(seed: Long)(implicit controller: Controller, app: Application) extends Component {
    var opened = false
    val random = new Random(seed)
    lazy val pixiContainer = new Container()
    lazy val pixiFlowers = (0 until random.between(FlowerClusterMinCount, FlowerClusterMaxCount)).map { index =>
      new Flower(random.oneOf(flowers)).withPixi { pixi =>
        val angle = random.nextDouble() * Math.PI * 2
        val distance = random.nextDouble() * FlowerClusterSpread
        val x = math.cos(angle) * distance
        val y = math.sin(angle) * distance
        pixi.addTo(pixiContainer).positionAt(x.toInt xy y.toInt)
      }
    }

    def open(): Unit = if (!opened) {
      pixiFlowers.foreach { flower =>
        flower.open(random.between(0, FlowerAnimationDelay))
      }
      opened = true
    }

    override def toPixi: DisplayObject = pixiContainer
  }

}