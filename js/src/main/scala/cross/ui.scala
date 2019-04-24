package cross

import cross.animation._
import cross.component._
import cross.global.GlobalContext
import cross.imp._
import cross.logging.Logging
import cross.mvc._
import cross.pixi._
import cross.stage._
import cross.vec._
import org.scalajs.dom._

import scala.concurrent.Future
import scala.scalajs.js.Dynamic.literal

//noinspection TypeAnnotation
object ui extends GlobalContext with Logging {

  case class UI(app: Application)

  def load(implicit controller: Controller): Future[UI] = Future {
    log.info("[ui] initializing...")
    val refreshScreenSize = () => controller.setScreenSize(window.innerWidth.toInt xy window.innerHeight.toInt)
    window.addEventListener("resize", (_: Event) => refreshScreenSize(), useCapture = false)
    refreshScreenSize()

    implicit val app = startPixi()
    bindLoaderLogs()
    bindStageTransitions()

    log.info("[ui] initialized")
    UI(app)
  }

  def startPixi()(implicit controller: Controller): Application = {
    val app = new Application(literal(
      width = 1,
      height = 1,
      antialias = true,
      transparent = false,
      resolution = 1
    ))
    app.renderer.backgroundColor = palette.Background
    app.renderer.view.style.position = "absolute"
    app.renderer.view.style.display = "block"
    app.renderer.autoResize = true
    controller.model.screen /> { case size => app.renderer.resize(size.x, size.y) }
    document.body.appendChild(app.view)
    app
  }

  def bindLoaderLogs()(implicit app: Application): Unit = {
    app.loader.on(EventType.Progress, { (l, r) =>
      log.info(s"[assets] loading [${r.url}], total progress [${l.progress}]")
    })
  }

  def bindStageTransitions()(implicit controller: Controller, app: Application): Unit = {
    val stageContainer = app.stage.sub
    val global = new GlobalStage()
    global.create
    global.toPixi.addTo(app.stage)
    var stage: Future[Stage] = Future.successful(new EmptyStage())
    val stages = Map(
      Stages.Loading -> new LoadingStage(),
      Stages.Game -> new GameStage()
    )
    controller.model.stage /> { case nextType =>
      val next = stages(nextType)
      stage = for {
        current <- stage
        _ = animation += current.fadeOut().onEnd(current.toPixi.detach)
        _ <- next.create
        _ = animation += next.fadeIn().onStart(stageContainer.addChild(next.toPixi))
      } yield next
    }
  }

  class EmptyStage extends Stage {
    private val container = new Container()
    override val create: Future[Unit] = Future.successful()
    override val fadeIn: Animation = EmptyAnimation
    override val fadeOut: Animation = EmptyAnimation
    override val toPixi: DisplayObject = container
  }

}