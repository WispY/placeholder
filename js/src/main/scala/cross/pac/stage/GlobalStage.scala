package cross.pac.stage

import cross.common._
import cross.component._
import cross.component.flat._
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

  lazy val pixiStage = new Container
  lazy val pixiBody = pixiStage.sub

  lazy val pixiWelcome = new Title("Poku Art Challenge", config.welcomeStyle).addTo(pixiBody)
  lazy val pixiUser = new Title("", config.userStyle).addTo(pixiBody)
  lazy val pixiLogin = new Button("Sign In", config.loginStyle, config.loginSize).addTo(pixiBody)

  lazy val layout = screenLayout.withChildren(
    YBox().fillBoth.withChildren(
      XBox().alignTop.fixedHeight(config.stageHeight).pad(config.stagePad).space(config.stageSpace).withChildren(
        Region(pixiBody).color(Some(Colors.Red)).fixedSize(100 xy 0).fillY,
        Filler,
        Region(pixiBody).color(Some(Colors.Green)).fixedSize(50 xy 0).fillY,
        Region(pixiBody).color(Some(Colors.Blue)).fixedSize(100 xy 0).fillY
      ),
      Filler
    )
  )

  override lazy val create: Future[Unit] = Future {
    log.info("setting up...")

    pixiWelcome.addTo(pixiBody).withPixi { pixi =>
      pixi.anchorAt(Vec2d.Left)
      pixi.positionAt(config.stagePad.x xy config.stageHeightHalf)
    }

    pixiUser.addTo(pixiBody).withPixi { pixi =>
      pixi.anchorAt(Vec2d.Right)
      pixi.positionAt(-(config.stagePad.x + config.stageSpace.x + config.loginSize.x) xy config.stageHeightHalf)
    }
    controller.model.username /> { case name => pixiUser.setText(name) }

    pixiLogin.addTo(pixiBody).withPixi { pixi =>
      pixi.positionAt((-config.stagePad.x - config.loginSize.x * 0.5) xy config.stageHeightHalf)
    }.onClick { button =>
      button.disable()
      button.setLabel("Loading")
      controller.login()
    }

    layout

    log.info("created")
  }

  override def fadeIn(): Animation = pixiBody.fadeIn

  override def fadeOut(): Animation = pixiBody.fadeOut

  override val toPixi: DisplayObject = pixiStage
}