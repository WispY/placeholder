package cross

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.IllegalRequestException
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.typesafe.scalalogging.LazyLogging
import cross.common._
import cross.config.JvmReader
import cross.general.config.GeneralConfig
import cross.general.routes.generalRoutes
import cross.general.session.{SessionManager, SessionManagerRef}
import cross.pac.bot.ArtChallengeBot
import cross.pac.config.PacConfig
import cross.pac.processor.ArtChallengeProcessor
import cross.pac.routes.pacRoutes
import cross.pac.thumbnailer.Thumbnailer

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

/** Launches the server */
object launcher extends App with LazyLogging {
  config.setGlobalReader(JvmReader)

  implicit val generalConfig: GeneralConfig = general.config.Config
  implicit val pacConfig: PacConfig = pac.config.Config

  implicit val system: ActorSystem = ActorSystem("akka")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val execution: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = 10.seconds

  implicit val sessionManager: SessionManagerRef = SessionManagerRef(system.actorOf(Props(new SessionManager()), "general.sessions"))

  val pacBot = system.actorOf(Props(new ArtChallengeBot()), "pac.bot")
  val pacThumbnailer = system.actorOf(Props(new Thumbnailer(materializer)), "pac.thumbnailer")
  val pacProcessor = system.actorOf(Props(new ArtChallengeProcessor(pacBot, pacThumbnailer)), "pac.processor")

  implicit val exceptionHandler: ExceptionHandler = ExceptionHandler {
    case IllegalRequestException(info, status) => complete(status, Some(info.detail).filter(_.nonEmpty).getOrElse(status.defaultMessage))
  }

  /** Routes list */
  val routes = generalRoutes() ++ pacRoutes(pacProcessor) ++ Nil
  val route: Route = Route.seal {
    cors() {
      concat(routes: _*)
    }
  }

  val (host, port) = (generalConfig.host, generalConfig.port)
  val binding = Http()
    .bindAndHandle(route, host, port)
    .whenFailed { up =>
      logger.error(s"failed to bind the server to [$host:$port]", up)
      stop()
    }
    .whenSuccessful { bind =>
      logger.info(s"server online at http://$host:$port/")
    }

  /** Terminates the application */
  def stop(): Unit = {
    system.terminate()
    binding.flatMap(b => b.unbind())
  }

  sys.addShutdownHook(stop())
}