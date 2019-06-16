package cross.pac.stage

import cross.common._
import cross.component.Stage
import cross.component.flat.Button
import cross.component.util.{DefaultFontStyle, Roboto}
import cross.ops._
import cross.pac.mvc.Controller
import cross.pixi._
import cross.util.animation.Animation
import cross.util.global.GlobalContext
import cross.util.logging.Logging
import org.scalajs.dom.window

import scala.concurrent.Future

//noinspection TypeAnnotation
class ArtChallengeStage()(implicit controller: Controller, app: Application) extends Stage with Logging with GlobalContext {
  lazy val pixiStage = centerStage
  lazy val pixiBody = pixiStage.sub
  lazy val pixiButton = new Button(label = "Hello, World!", size = 150 xy 50, fontStyle = DefaultFontStyle.copy(font = Roboto))

  override lazy val create: Future[Unit] = Future {
    log.info("[art challenge stage] setting up...")
    pixiButton.addTo(pixiBody).onClick { button =>
      window.location.href = "https://discordapp.com/api/oauth2/authorize?client_id=583316882002673683&redirect_uri=http://127.0.0.1:8080/discord&response_type=code&scope=identify"
    }
    log.info("[art challenge stage] created")
  }

  override def fadeIn(): Animation = pixiBody.fadeIn

  override def fadeOut(): Animation = pixiBody.fadeOut

  override val toPixi: DisplayObject = pixiStage
}