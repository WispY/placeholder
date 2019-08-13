package cross.pac

import cross.common._
import cross.general.config.GeneralConfig
import cross.general.protocol.User
import cross.pac.config.PacConfig
import cross.pac.protocol.{ArtChallenge, ChatMessage}
import cross.util.global.GlobalContext
import cross.util.http
import cross.util.logging.Logging
import cross.util.mvc.{GenericController, GenericModel}
import cross.util.timer.Timer

import scala.concurrent.{ExecutionContext, Future}

object mvc extends GlobalContext with Logging {
  override protected def logKey: String = "pac/mvc"

  /** Contains logic for pac project */
  class Controller(val model: Model)(implicit generalConfig: GeneralConfig, config: PacConfig) extends GenericController[Unit] {
    val timer = new Timer()

    /** Initializes the controller */
    def start(path: String): Future[Unit] = for {
      _ <- UnitFuture
      _ = log.info("starting...")
      _ = timer.start(60, () => model.tick.write(model.tick() + 1))
      _ = bind()
      _ <- readCurrentUser()
      _ = loadPage(path)
      _ = log.info("started")
    } yield ()

    /** Opens page taken from the path */
    private def loadPage(path: String): Unit = {
      log.info(s"loading [$path]")
      path match {
        case manage if manage.endsWith("/manage") =>
          if (isAdmin) {
            managePage()
          } else if (isSignedIn) {
            artChallengesPage()
          } else {
            signIn()
          }
        case _ =>
          artChallengesPage()
      }
    }

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
      _ = log.info("reading current user")
      userOpt <- Future.successful(None) // http.get[Option[User]]("/api/user")
      _ = model.user.write(userOpt)
      _ = log.info(s"current user is [$userOpt]")
    } yield ()

    /** Returns true if the user has signed in */
    private def isSignedIn: Boolean = model.user().isDefined

    /** Returns true if the user has signed in and is an admin */
    private def isAdmin: Boolean = model.user().exists(u => u.admin)

    /** Redirects to art challenges page */
    def artChallengesPage(): Unit = {
      log.info("redirecting to art challenges")
      model.page.write(Pages.ArtChallenges)
    }

    /** Redirects to manage stage */
    def managePage(): Unit = {
      log.info("redirecting to manage stage")
      model.page.write(Pages.Manage)
      // http.get[List[ArtChallenge]]("/api/pac/challenges")
      //   .whenSuccessful(list => log.info(s"loaded [${list.size}] challenges"))
      //   .whenFailed(up => log.error("failed to read challenges", up))
      //   .load(model.artChallenges, list => list, "Failed to load the list of art challenged")
      // http.get[List[ChatMessage]]("/api/pac/admin-messages")
      //   .whenSuccessful(list => log.info(s"loaded [${list.size}] admin messages"))
      //   .whenFailed(up => log.error("failed to read admin messages", up))
      //   .load(model.adminMessages, list => list.sortBy(m => -m.createTimestamp), "Failed to load the list of messages")
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
                   user: Writeable[Option[User]] = Data(None),
                   artChallenges: Writeable[Loadable[List[ArtChallenge]]] = Data(None),
                   adminMessages: Writeable[Loadable[List[ChatMessage]]] = Data(None)) extends GenericModel[Unit]

  /** Stages for pac project */
  object Pages extends Enumeration {
    val ArtChallenges, GalleryView, SubmissionView, Manage = Value
  }

  type Loadable[A] = Option[Either[String, A]]

  implicit class LoadableOps[A](val loadable: Writeable[Loadable[A]]) extends AnyVal {
    /** Resets the loadable to loading status */
    def reset(): Unit = loadable.write(None)

    /** Loads the value of the loadable from the future */
    def load[B](future: Future[B], mapping: B => A, error: String)(implicit ec: ExecutionContext): Unit = {
      future
        .whenSuccessful(b => loadable.write(Some(Right(mapping.apply(b)))))
        .whenFailed(up => loadable.write(Some(Left(error))))
    }

    /** Executes given code on load */
    def onLoad(code: A => Unit): Writeable[Loadable[A]] = {
      loadable /> { case Some(Right(a)) => code.apply(a) }
      loadable
    }

    /** Executes given code on loading */
    def onLoading(code: => Unit): Writeable[Loadable[A]] = {
      loadable /> { case None => code }
      loadable
    }

    /** Executes given code on failure */
    def onFailed(code: String => Unit): Writeable[Loadable[A]] = {
      loadable /> { case Some(Left(error)) => code.apply(error) }
      loadable
    }

    /** Creates a view of the loadable value with a default if failed or loading */
    def view(default: A): Data[A] = loadable.map {
      case Some(Right(a)) => a
      case _ => default
    }
  }

  implicit class MvcFutureOps[A](val future: Future[A]) extends AnyVal {
    /** Loads the future into the loadable */
    def load[B](loadable: Writeable[Loadable[B]], mapping: A => B, error: String): Future[A] = {
      loadable.reset()
      loadable.load(future, mapping, error)
      future
    }
  }

}