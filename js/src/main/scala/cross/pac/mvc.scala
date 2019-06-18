package cross.pac

import cross.common._
import cross.general.config.GeneralConfig
import cross.general.protocol.User
import cross.pac.config.PacConfig
import cross.util.global.GlobalContext
import cross.util.http
import cross.util.logging.Logging
import cross.util.mvc.{GenericController, GenericModel}
import cross.util.timer.Timer

import scala.concurrent.Future

object mvc extends GlobalContext with Logging {
  override protected def logKey: String = "pac/mvc"

  /** Contains logic for pac project */
  class Controller(val model: Model)(implicit generalConfig: GeneralConfig, config: PacConfig) extends GenericController[Unit] {
    val timer = new Timer()

    /** Initializes the controller */
    def start(): Future[Unit] = for {
      _ <- UnitFuture
      _ = log.info("[controller] starting...")
      _ = timer.start(60, () => model.tick.write(model.tick() + 1))
      _ = bind()
      _ <- readCurrentUser()
      _ = log.info("[controller] started")
    } yield ()

    /** Binds internal model logic */
    private def bind(): Unit = {
      model.page /> { case page =>
        val path = page match {
          case Pages.ArtChallenges => "/pac"
          case Pages.GalleryView => "/pac/gallery"
          case Pages.SubmissionView => "/pac/submission"
          case Pages.Manage => "/pac/manage"
        }
        val title = page match {
          case Pages.ArtChallenges => "Home"
          case Pages.GalleryView => "Gallery"
          case Pages.SubmissionView => "Submission"
          case Pages.Manage => "Manage"
        }
        http.updateTitle(s"Poku Art Challenge - $title")
        http.redirectSilent(path)
      }
    }

    /** Reads current user from the server */
    private def readCurrentUser(): Future[Unit] = for {
      _ <- UnitFuture
      _ = log.info("[controller] reading current user")
      userOpt <- http.get[Option[User]]("/api/user")
      _ = model.user.write(userOpt)
      _ = log.info(s"[controller] current user is [$userOpt]")
    } yield ()

    /** Redirects to art challenges page */
    def artChallenges(): Unit = {
      log.info("redirecting to art challenges")
      model.page.write(Pages.ArtChallenges)
    }

    /** Redirects to manage stage */
    def manage(): Unit = {
      log.info("redirecting to manage stage")
      model.page.write(Pages.Manage)
    }

    /** Redirects to discord login page */
    def signIn(): Unit = {
      log.info(s"redirecting to discord login [${generalConfig.discordLogin}]")
      http.redirectFull(generalConfig.discordLogin)
    }

    /** Signs out the user */
    def signOut(): Unit = {
      log.info("signing out")
      http.postUnit[Unit]("/api/signout", ()).foreach(_ => model.user.write(None))
    }

    /** Updates the rendering screen size */
    override def setScreenSize(size: Vec2i): Unit = model.screen.write(size)

    /** Updates the global mouse position on the screen */
    override def setMousePosition(mouse: Vec2d): Unit = model.mouse.write(mouse)
  }

  /** Contains data for pac project */
  case class Model(tick: Writeable[Long] = Data(0),
                   screen: Writeable[Vec2i] = Data(0 xy 0),
                   scale: Writeable[Double] = Data(1.0),
                   stage: Writeable[Unit] = Data(),
                   page: Writeable[Pages.Value] = Data(Pages.ArtChallenges),
                   mouse: Writeable[Vec2d] = Data(Vec2d.Zero),
                   user: Writeable[Option[User]] = Data(None)) extends GenericModel[Unit]

  /** Stages for pac project */
  object Pages extends Enumeration {
    val ArtChallenges, GalleryView, SubmissionView, Manage = Value
  }

}