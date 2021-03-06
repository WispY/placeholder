package cross.sakura.stage

import cross.common._
import cross.component.Stage
import cross.component.pixel.Button
import cross.ops._
import cross.pixi._
import cross.sakura.asset.flower._
import cross.sakura.asset.tree._
import cross.sakura.asset.ui._
import cross.sakura.mvc.Controller
import cross.util.animation.Animation
import cross.util.global.GlobalContext
import cross.util.logging.Logging
import cross.util.spring
import cross.util.spring.DoubleSpring

import scala.concurrent.{Future, Promise}

//noinspection TypeAnnotation
class LoadingStage()(implicit controller: Controller, app: Application) extends Stage with Logging with GlobalContext {
  override protected def logKey: String = "sakura/loading"
  lazy val pixiStage = centerStage
  lazy val pixiBody = pixiStage.sub
  lazy val pixiStart = Button(`asset-start-normal`, `asset-start-hover`, `asset-start-pressed`, `asset-start-normal`)
  val buttonSize = 48 xy 16

  override lazy val create: Future[Unit] = {
    log.info("pre-loading...")
    val promise = Promise[Unit]
    (`asset-loading-disabled` :: `asset-loading-normal` :: Nil)
      .resetAndAddToLoader
      .load({ () =>
        log.info("setting up...")

        val start = `asset-loading-disabled`.sprite
          .anchorAtCenter
          .addTo(pixiBody)
        val finish = `asset-loading-normal`.sprite
          .anchorAtCenter
          .addTo(pixiBody)
          .maskWith {
            new Graphics()
              .fillRect(buttonSize)
              .pivotAt(buttonSize * Vec2d.Bottom)
              .positionAt(0.0 xy (buttonSize.y * 0.5))
              .scaleYTo(0)
              .addTo(pixiBody)
          }

        val progressSpring = spring.add(DoubleSpring(0, 0, { s =>
          finish.mask.scale.y = (s.current * buttonSize.y).toInt / buttonSize.y.toDouble
          if (s.current > 0.99) {
            pixiStart.onClick { button =>
              button.setEnabled(false)
              controller.jumpToGame()
            }.toPixi.addTo(pixiBody)
            finish.mask.scale.y = 1
            finish.visible = false
            start.visible = false
            spring.remove(s)
          }
        }, 0.25))

        val allAssets = all ++ branches.map(b => b.asset) ++ flowers.flatten
        allAssets
          .resetAndAddToLoader
          .on(EventType.Progress, { (load, res) =>
            progressSpring.target = load.progress / 100
          })
          .load({ () =>
            log.info("assets loaded")
            nextFrame {
              val buffer = allAssets.map { asset =>
                asset.sprite.positionAt(1000 xy 1000).addTo(pixiBody)
              }
              nextFrame {
                buffer.foreach(sprite => sprite.detach)
                nextFrame {
                  controller.markLoaded()
                }
              }
            }
          })
        log.info("created")
        promise.success()
      })
    promise.future
  }

  override def fadeIn(): Animation = pixiBody.fadeIn

  override def fadeOut(): Animation = pixiBody.fadeOut

  override val toPixi: DisplayObject = pixiStage
}
