package cross.stage

import cross.animation.{Animation, EmptyAnimation}
import cross.component.Stage
import cross.global.GlobalContext
import cross.imp._
import cross.logging.Logging
import cross.mvc.Controller
import cross.pixi.{Application, DisplayObject}

import scala.concurrent.Future

//noinspection TypeAnnotation
class GlobalStage()(implicit controller: Controller, app: Application) extends Stage with Logging with GlobalContext {
  lazy val pixiStage = topLeftStage

  override lazy val create: Future[Unit] = Future {
    log.info("[global stage] setting up...")
    log.info("[global stage] created...")
  }

  override def fadeIn(): Animation = EmptyAnimation

  override def fadeOut(): Animation = EmptyAnimation

  override val toPixi: DisplayObject = pixiStage
}