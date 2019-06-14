package cross

import cross.component.util._
import cross.pac.stage.ArtChallengeStage
import cross.pixi.{ScaleModes, Settings}
import cross.sakura.stage.{GameStage, LoadingStage}
import cross.util.global.GlobalContext
import cross.util.logging.Logging
import cross.util.mvc.Ui
import cross.util.{animation, fonts, spring}
import org.scalajs.dom._

import scala.concurrent.ExecutionContext

/** Starts the UI application */
//noinspection TypeAnnotation
object app extends App with GlobalContext with Logging {
  window.location.pathname match {
    case sakura if sakura.startsWith("/sakura") =>
      startSakura()
    case pac if pac.startsWith("/pac") =>
      startPac()
    case _ =>
      log.info("[app] redirecting to [/pac]")
      window.location.href = "/pac"
  }

  def startSakura(): Unit = {
    import cross.sakura.mvc._

    log.info("[app] starting sakura project")
    Settings.SCALE_MODE = ScaleModes.NEAREST
    document.title = "Sakura Challenge"
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

    log.info("[app] starting pac project")
    document.title = "Poku Art Challenge"
    implicit val model = Model()
    implicit val controller = new Controller(model)
    implicit val ec = ExecutionContext.global

    for {
      _ <- fonts.load(Roboto :: RobotoSlab :: Nil)
      _ <- new Ui[Stages.Value]({ (stage, application) =>
        implicit val app = application
        stage match {
          case Stages.ArtChallenges => new ArtChallengeStage()
        }
      }).load()
      _ <- spring.load()
      _ <- animation.load()
      _ <- controller.start()
    } yield ()
  }

}