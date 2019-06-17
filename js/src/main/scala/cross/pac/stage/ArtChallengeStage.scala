package cross.pac.stage

import cross.component.Stage
import cross.ops._
import cross.pac.mvc.Controller
import cross.pixi._
import cross.util.animation.Animation
import cross.util.global.GlobalContext
import cross.util.logging.Logging

import scala.concurrent.Future

//noinspection TypeAnnotation
class ArtChallengeStage()(implicit controller: Controller, app: Application) extends Stage with Logging with GlobalContext {
  override protected def logKey: String = "pac/challenge"

  lazy val pixiStage = centerStage
  lazy val pixiBody = pixiStage.sub

  override lazy val create: Future[Unit] = Future {
    log.info("setting up...")
    log.info("created")
  }

  override def fadeIn(): Animation = pixiBody.fadeIn

  override def fadeOut(): Animation = pixiBody.fadeOut

  override val toPixi: DisplayObject = pixiStage
}