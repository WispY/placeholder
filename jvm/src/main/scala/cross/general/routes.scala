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
import cross.common.UnitFuture
import cross.general.config._
import cross.general.session.{ForgetSession, Session, SessionManagerRef, UpdateSession}
import cross.pac.config.PacConfig
import cross.routes._
import cross.util.akkautil._
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.concurrent.ExecutionContext

object routes extends SprayJsonSupport with LazyLogging {

  /** Contains all of the projects configs */
  case class FullConfig(general: GeneralConfig = cross.general.config.Config,
                        pac: PacConfig = cross.pac.config.Config)

  implicit val fullConfigFormat: RootJsonFormat[FullConfig] = jsonFormat2(FullConfig)

  /** Returns general routes */
  def generalRoutes()(implicit config: GeneralConfig, system: ActorSystem, manager: SessionManagerRef, materializer: Materializer, ec: ExecutionContext): List[Route] = List(
    /** Returns 200 OK */
    `GET /api/health` {
      complete(HttpResponse(StatusCodes.OK))
    },

    /** Returns current server configs only for admins */
    `GET /api/config`.apply { session =>
      complete(FullConfig())
    },

    /** Mock for websockets */
    `GET /api/ws` {
      complete(HttpResponse(StatusCodes.OK))
    },

    /** Returns currently logged in user for browser session */
    `GET /api/user`.apply { session =>
      complete(session.discordUser.map(u => u.asUser))
    },

    /** Authorizes the user via discord using grant code */
    `POST /api/discord`.apply { (session, login) =>
      implicit val to: Timeout = Timeout.durationToTimeout(config.timeout)
      onSuccess(for {
        _ <- UnitFuture
        _ = logger.info("authorizing discord user")
        auth <- discord.authorize(login.code, config)
        _ = logger.info("reading discord user data")
        discordUser <- discord.selfUser(auth)
        _ = logger.info(s"successfully authorized as [$discordUser], updating session")
        updated <- (manager.ref ? UpdateSession(session.id, s => s.copy(discordUser = Some(discordUser)))).mapTo[Session]
        sessionId = updated.id
      } yield (discordUser, sessionId)) { (discordUser, sessionId) =>
        resetSession(sessionId) {
          complete(discordUser.asUser)
        }
      }
    },

    /** Signs out the user by refreshing the session */
    `POST /api/signout`.apply { session =>
      implicit val to: Timeout = Timeout.durationToTimeout(config.timeout)
      onSuccess(for {
        refreshed <- (manager.ref ? ForgetSession(session.id)).mapTo[Session]
      } yield refreshed.id) { sessionId =>
        resetSession(sessionId) {
          complete()
        }
      }
    }
  )
}