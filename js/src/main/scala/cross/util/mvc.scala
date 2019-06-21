package cross.util

import cross.common.{Vec2i, Writeable, _}
import cross.component._
import cross.component.util.Colors
import cross.ops._
import cross.pixi._
import cross.util.animation._
import cross.util.global.GlobalContext
import cross.util.logging.Logging
import org.scalajs.dom._

import scala.concurrent.Future
import scala.scalajs.js.Dynamic.literal

object mvc {

  /** Defines controller with common functionality */
  trait GenericController[A] {
    /** Returns the ref to app model */
    def model: GenericModel[A]

    /** Updates the rendering screen size */
    def setScreenSize(size: Vec2i): Unit

    /** Updates the global mouse position on the screen */
    def setMousePosition(mouse: Vec2d): Unit
  }

  /** Defines model with common fields */
  trait GenericModel[A] {
    /** Returns the current update tick */
    def tick: Writeable[Long]

    /** Returns the current screen size */
    def screen: Writeable[Vec2i]

    /** Returns the current screen scale */
    def scale: Writeable[Double]

    /** Returns the current application stage/screen */
    def stage: Writeable[A]

    /** Returns current mouse coordinates */
    def mouse: Writeable[Vec2d]
  }

  class Ui[A](stages: (A, Application) => Stage, global: Application => Option[Stage] = _ => None)(implicit controller: GenericController[A]) extends Logging with GlobalContext {
    override protected def logKey: String = "ui"

    /** Loads the application UI */
    def load(): Future[Unit] = Future {
      log.info("[ui] initializing...")
      val refreshScreenSize = () => controller.setScreenSize(window.innerWidth.toInt xy window.innerHeight.toInt)
      window.addEventListener("resize", (_: Event) => refreshScreenSize(), useCapture = false)
      refreshScreenSize()

      implicit val app: Application = startPixi()
      bindLoaderLogs()
      bindStageTransitions()

      log.info("[ui] initialized")
    }

    private def startPixi(): Application = {
      val app = new Application(literal(
        width = 1,
        height = 1,
        antialias = true,
        transparent = false,
        resolution = 1
      ))
      app.renderer.backgroundColor = Colors.Black.toDouble
      app.renderer.view.style.position = "absolute"
      app.renderer.view.style.display = "block"
      app.renderer.autoResize = true
      controller.model.screen /> { case size => app.renderer.resize(size.x, size.y) }
      controller.model.tick /> { case tick => controller.setMousePosition(app.renderer.plugins.interaction.mouse.global) }
      document.body.appendChild(app.view)
      app
    }

    private def bindLoaderLogs()(implicit app: Application): Unit = {
      app.loader.on(EventType.Progress, { (l, r) =>
        log.info(s"[assets] loading [${r.url}], total progress [${l.progress}]")
      })
    }

    private def bindStageTransitions()(implicit app: Application): Unit = {
      val stageContainer = app.stage.sub
      global.apply(app).foreach { stage =>
        stage.create
        stage.toPixi.addTo(app.stage)
      }
      var stage: Future[Stage] = Future.successful(new EmptyStage())
      controller.model.stage /> { case nextType =>
        val next = stages.apply(nextType, app)
        stage = for {
          current <- stage
          _ = animation += current.fadeOut().onEnd(current.toPixi.detach)
          _ <- next.create
          _ = animation += next.fadeIn().onStart(stageContainer.addChild(next.toPixi))
        } yield next
      }
    }
  }

  /** Represents an application stage without any objects */
  class EmptyStage extends Stage {
    private val container = new Container()
    override val create: Future[Unit] = UnitFuture
    override val fadeIn: Animation = EmptyAnimation
    override val fadeOut: Animation = EmptyAnimation
    override val toPixi: DisplayObject = container
  }

}