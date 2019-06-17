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
  class Controller(val model: Model)(implicit generalConfig: GeneralConfig, config: PacConfig) extends GenericController[Stages.Value] {
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
      model.user /~ { case Some(user) => user.name } /> { case nameOpt => model.username.write(nameOpt.getOrElse("Guest")) }
    }

    /** Reads current user from the server */
    private def readCurrentUser(): Future[Unit] = for {
      _ <- UnitFuture
      _ = log.info("[controller] reading current user")
      userOpt <- http.get[Option[User]]("/api/user")
      _ = model.user.write(userOpt)
      _ = log.info(s"[controller] current user is [$userOpt]")
    } yield ()

    /** Redirects to discord login page */
    def login(): Unit = {
      log.info(s"redirecting to discord login [${generalConfig.discordLogin}]")
      http.redirectFull(generalConfig.discordLogin)
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
                   stage: Writeable[Stages.Value] = Data(Stages.ArtChallenges),
                   mouse: Writeable[Vec2d] = Data(Vec2d.Zero),
                   user: Writeable[Option[User]] = Data(None),
                   username: Writeable[String] = Data("Loading...")) extends GenericModel[Stages.Value]

  /** Stages for pac project */
  object Stages extends Enumeration {
    val ArtChallenges, GalleryView, SubmissionView = Value
  }

}