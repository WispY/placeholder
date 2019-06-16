package cross

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.concat
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.typesafe.scalalogging.LazyLogging
import cross.common._
import cross.config.JvmReader
import cross.general.session.{SessionManager, SessionManagerRef}
import cross.pac.bot.ArtChallengeBot
import cross.pac.processor.ArtChallengeProcessor
import cross.pac.thumbnailer.Thumbnailer

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

/** Launches the server */
object launcher extends App with LazyLogging {
  config.setGlobalReader(JvmReader)

  implicit val system: ActorSystem = ActorSystem("akka")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val execution: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = 10.seconds

  implicit val sessionManager: SessionManagerRef = SessionManagerRef(system.actorOf(Props(new SessionManager()), "general.sessions"))

  val pacBot = system.actorOf(Props(new ArtChallengeBot(pac.config.Config)), "pac.bot")
  val pacThumbnailer = system.actorOf(Props(new Thumbnailer(materializer, pac.config.Config)), "pac.thumbnailer")
  val pacProcessor = system.actorOf(Props(new ArtChallengeProcessor(pacBot, pacThumbnailer, pac.config.Config)), "pac.processor")

  val routes = cross.general.routes.get(general.config.Config) ++ Nil
  val route: Route = cors()(concat(routes: _*))

  val (host, port) = (general.config.Config.host, general.config.Config.port)
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