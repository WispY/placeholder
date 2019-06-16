package cross.general

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import cross.general.config._
import cross.general.protocol.User
import cross.general.session.{Session, SessionManagerRef, UpdateSession}
import cross.pac.config.PacConfig
import cross.routes._
import cross.util.akkautil._
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.concurrent.{ExecutionContext, Future}

object routes extends SprayJsonSupport with LazyLogging {

  /** Contains all of the projects configs */
  case class FullConfig(general: GeneralConfig = cross.general.config.Config,
                        pac: PacConfig = cross.pac.config.Config)

  implicit val fullConfigFormat: RootJsonFormat[FullConfig] = jsonFormat2(FullConfig)

  /** Returns general routes */
  def get(config: GeneralConfig)(implicit system: ActorSystem, manager: SessionManagerRef, materializer: Materializer, ec: ExecutionContext): List[Route] = List(
    `GET /api/health` {
      complete(HttpResponse(StatusCodes.OK))
    },
    `GET /api/config` {
      complete(FullConfig())
    },
    `GET /api/ws` {
      complete(HttpResponse(StatusCodes.OK))
    },
    `POST /api/discord`.apply { (session, login) =>
      implicit val to: Timeout = Timeout.durationToTimeout(config.timeout)
      onSuccess(for {
        _ <- Future.successful()
        _ = logger.info("authorizing discord user")
        auth <- discord.authorize(login.code, config)
        _ = logger.info("reading discord user data")
        discordUser <- discord.selfUser(auth)
        _ = logger.info(s"successfully authorized as [$discordUser], updating session")
        updated <- (manager.ref ? UpdateSession(session.id, s => s.copy(discordUser = Some(discordUser)))).mapTo[Session]
        user = User(discordUser.id, discordUser.username)
        sessionId = updated.id
      } yield (user, sessionId)) { (user, sessionId) =>
        resetSession(sessionId) {
          complete(user)
        }
      }
    }
  )
}