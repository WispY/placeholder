package cross.pac.stage

import cross.component._
import cross.component.layout._
import cross.general.config.GeneralConfig
import cross.layout._
import cross.ops._
import cross.pac.config.PacConfig
import cross.pac.mvc.{Controller, Pages}
import cross.pixi._
import cross.util.animation.Animation
import cross.util.global.GlobalContext
import cross.util.logging.Logging

import scala.concurrent.Future

class PacStage(implicit generalConfig: GeneralConfig, config: PacConfig, controller: Controller, app: Application) extends Stage with Logging with GlobalContext {
  override protected def logKey: String = "pac/stage"

  private lazy val stage = new Container
  private lazy val body = stage.sub

  private lazy val bar = region(config.barBackground)
  private lazy val shadow = region(config.barShadow)
  private lazy val welcome = button(config.welcomeButtonStyle).children(label("Poku Art Challenge", config.welcomeLabelStyle))
  private lazy val user = label("", config.userStyle)
  private lazy val signinLabel = label("Sign In", config.signinLabelStyle)
  private lazy val signin = button(config.signinButtonStyle).children(signinLabel)
  private lazy val manage = button(config.manageButtonStyle).children(label("Manage", config.manageLabelStyle))
  private lazy val manageContent = {
    val buttons = (0 until 50).map { i =>
      button(config.signinButtonStyle).fillX.pad(15).children(fillLabel(s"Art Challenge - Art Challenge - Art Challenge $i", config.signinLabelStyle))
    }
    ybox.pad(20).space(10).children(buttons: _*).alignTop.fillX
  }
  private lazy val managePage = scroll(config.manageScroll).view(manageContent).alignTop

  private lazy val pages = box.children(managePage).fillBoth

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
        shadow.fillX.alignTop.height(config.barShadowSize),
        pages
      )
    )
    .layout()

  override lazy val create: Future[Unit] = Future {
    log.info("setting up...")
    pages :: shadow :: bar :: welcome :: user :: manage :: signin :: signinLabel :: layout :: Nil

    layout.componentsIn(body)

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

    controller.model.page /> { case page =>
      pages.getImmediateChildren.foreach(c => c.visible(false))
      page match {
        case Pages.Manage => managePage.visible(true)
        case _ =>
      }
    }

    log.info("created")
  }

  override def fadeIn(): Animation = body.fadeIn

  override def fadeOut(): Animation = body.fadeOut

  override def toPixi: DisplayObject = stage
}