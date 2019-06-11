package cross.sakura.stage

import cross.component.Stage
import cross.ops._
import cross.pixi.{Application, DisplayObject}
import cross.sakura.mvc.Controller
import cross.util.animation.{Animation, EmptyAnimation}
import cross.util.global.GlobalContext
import cross.util.logging.Logging

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