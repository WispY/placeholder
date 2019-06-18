package cross.pac.stage

import cross.component._
import cross.component.layout._
import cross.layout._
import cross.ops._
import cross.pac.config.PacGlobalStageConfig
import cross.pac.mvc.Controller
import cross.pixi._
import cross.util.animation.Animation
import cross.util.global.GlobalContext
import cross.util.logging.Logging

import scala.concurrent.Future

class PacStage(implicit config: PacGlobalStageConfig, controller: Controller, app: Application) extends Stage with Logging with GlobalContext {
  override protected def logKey: String = "pac/stage"

  private lazy val stage = new Container
  private lazy val body = stage.sub

  private lazy val bar = body.region(config.stageColor)
  private lazy val shadow = body.region(config.stageShadow)
  private lazy val welcome = body.button(config.welcomeButtonStyle).children(body.label("Poku Art Challenge", config.welcomeLabelStyle))
  private lazy val user = body.label("", config.userStyle)
  private lazy val signinLabel = body.label("Sign In", config.signinLabelStyle)
  private lazy val signin = body.button(config.signinButtonStyle).children(signinLabel)
  private lazy val manage = body.button(config.manageButtonStyle).children(body.label("Manage", config.manageLabelStyle))
  private lazy val contentScroll = body.scroll()

  private lazy val layout = screenLayout
    .children(
      ybox.fillBoth.children(
        bar.fillX.alignTop.children(
          xbox.pad(config.stagePad).space(config.stageSpace).children(
            welcome.pad(config.stagePad).fillY,
            manage.pad(config.stagePad).fillY,
            filler,
            user.pad(config.stagePad).fillY,
            signin.pad(config.stagePad).fillY
          )
        ),
        shadow.fillX.alignTop.height(config.stageShadowSize),
        contentScroll.content { case (content, contentLayout) =>
          val buttons = (0 until 50).map { i =>
            content.button(config.signinButtonStyle).pad(15).children(content.label(s"Art Challenge $i", config.signinLabelStyle))
          }
          contentLayout.children(
            ybox.pad(20).space(10).children(buttons: _*)
          )
        },
        filler
      )
    )
    .layout()

  override lazy val create: Future[Unit] = Future {
    log.info("setting up...")
    shadow :: bar :: welcome :: user :: manage :: signin :: signinLabel :: layout :: Nil

    controller.model.user /> {
      case Some(u) =>
        manage.visible(u.admin)
        signinLabel.label("Sign Out")
        signin.enable().onClick { _ =>
          signin.disable()
          signinLabel.label("Loading")
          controller.signOut()
        }
        user.label(u.name)
      case None =>
        manage.visible(false)
        signinLabel.label("Sign In")
        signin.enable().onClick { _ =>
          signin.disable()
          signinLabel.label("Loading")
          controller.signIn()
        }
        user.label("Guest")
    }

    welcome.onClick(_ => controller.artChallenges())

    manage.onClick(_ => controller.manage())

    log.info("created")
  }

  override def fadeIn(): Animation = body.fadeIn

  override def fadeOut(): Animation = body.fadeOut

  override def toPixi: DisplayObject = stage
}