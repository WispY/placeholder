package cross.pac.stage

import cross.common._
import cross.component._
import cross.component.layout._
import cross.component.util.Colors
import cross.layout._
import cross.ops._
import cross.pac.config.PacGlobalStageConfig
import cross.pac.mvc.Controller
import cross.pixi._
import cross.util.animation.Animation
import cross.util.global.GlobalContext
import cross.util.logging.Logging

import scala.concurrent.Future

//noinspection TypeAnnotation
class GlobalStage(implicit config: PacGlobalStageConfig, controller: Controller, app: Application) extends Stage with Logging with GlobalContext {
  override protected def logKey: String = "pac/challenge"

  lazy val stage = new Container
  lazy val body = stage.sub

  // lazy val pixiWelcome = new Title("Poku Art Challenge", config.welcomeStyle).addTo(body)
  // lazy val pixiUser = new Title("", config.userStyle).addTo(body)
  // lazy val pixiLogin = new Button("Sign In", config.loginStyle, config.loginSize).addTo(body)

  lazy val layout = screenLayout
    .children(
      ybox.fillBoth.children(
        body.region(config.stageColor).fillX.alignTop.children(
          xbox.height(config.stageHeight).pad(config.stagePad).space(config.stageSpace).children(
            body.button(config.loginStyle).size(100 xy 0).fillY,
            filler,
            body.button(config.loginStyle.copy(colorNormal = Colors.Green, colorHover = Colors.GreenLight, colorPressed = Colors.Green)).size(50 xy 0).fillY,
            body.button(config.loginStyle.copy(colorNormal = Colors.Red, colorHover = Colors.PurpleLight, colorPressed = Colors.Red)).size(100 xy 0).fillY
          )
        ),
        filler
      )
    )
    .layout()

  override lazy val create: Future[Unit] = Future {
    log.info("setting up...")

    // pixiWelcome.addTo(body).withPixi { pixi =>
    //   pixi.anchorAt(Vec2d.Left)
    //   pixi.positionAt(config.stagePad.x xy config.stageHeightHalf)
    // }

    // pixiUser.addTo(body).withPixi { pixi =>
    //   pixi.anchorAt(Vec2d.Right)
    //   pixi.positionAt(-(config.stagePad.x + config.stageSpace.x + config.loginSize.x) xy config.stageHeightHalf)
    // }
    // controller.model.username /> { case name => pixiUser.setText(name) }

    // pixiLogin.addTo(body).withPixi { pixi =>
    //   pixi.positionAt((-config.stagePad.x - config.loginSize.x * 0.5) xy config.stageHeightHalf)
    // }.onClick { button =>
    //   button.disable()
    //   button.setLabel("Loading")
    //   controller.login()
    // }

    layout

    log.info("created")
  }

  override def fadeIn(): Animation = body.fadeIn

  override def fadeOut(): Animation = body.fadeOut

  override def toPixi: DisplayObject = stage
}