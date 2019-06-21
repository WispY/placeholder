package cross

import cross.component.util._
import cross.general.config.{GeneralConfig, JsReader}
import cross.general.protocol._
import cross.pac.stage.PacStage
import cross.pixi.{ScaleModes, Settings}
import cross.sakura.stage.{GameStage, LoadingStage}
import cross.util.global.GlobalContext
import cross.util.http._
import cross.util.logging.Logging
import cross.util.mvc.Ui
import cross.util.{animation, fonts, spring}
import org.scalajs.dom._

import scala.concurrent.ExecutionContext

/** Starts the UI application */
//noinspection TypeAnnotation
object app extends App with GlobalContext with Logging {
  override protected def logKey: String = "app"

  config.setGlobalReader(JsReader)
  implicit val generalConfig: GeneralConfig = general.config.Config

  window.location.pathname match {
    case sakura if sakura.startsWith("/sakura") =>
      startSakura()
    case pac if pac.startsWith("/pac") =>
      startPac()
    case discord if discord.startsWith("/discord") =>
      queryParameter("code") match {
        case Some(code) => loginDiscord(code)
        case None =>
          log.warn("login error, redirecting to [/pac]")
          redirect("/pac")
      }
    case _ =>
      log.info("redirecting to [/pac]")
      redirect("/pac")
  }

  def startSakura(): Unit = {
    import cross.sakura.mvc._

    log.info("starting sakura project")
    Settings.SCALE_MODE = ScaleModes.NEAREST
    updateTitle("Sakura Challenge")
    implicit val model = Model()
    implicit val controller = new Controller(model)
    implicit val ec = ExecutionContext.global

    for {
      _ <- new Ui[Stages.Value]({ (stage, application) =>
        implicit val app = application
        stage match {
          case Stages.Loading => new LoadingStage()
          case Stages.Game => new GameStage()
        }
      }).load()
      _ <- spring.load()
      _ <- animation.load()
      _ <- controller.start()
    } yield ()
  }

  def startPac(): Unit = {
    import cross.pac.mvc._

    log.info("starting pac project")
    updateTitle("Poku Art Challenge")
    implicit val pacConfig = pac.config.Config
    implicit val model = Model()
    implicit val controller = new Controller(model)
    implicit val ec = ExecutionContext.global

    for {
      _ <- fonts.load(Roboto :: RobotoSlab :: Nil)
      _ <- new Ui[Unit]({ (stage, application) =>
        implicit val app = application
        stage match {
          case _ => new PacStage()
        }
      }).load()
      _ <- spring.load()
      _ <- animation.load()
      _ <- controller.start()
    } yield ()
  }

  def loginDiscord(code: String): Unit = for {
    user <- post[LoginDiscord, User]("/api/discord", LoginDiscord(code))
    _ = log.info(s"logged in as [$user]")
    _ = redirectSilent("/pac", preserveQuery = false)
    _ = startPac()
  } yield user

}