package cross.pac

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import cross.common._
import cross.general.config.GeneralConfig
import cross.general.session.SessionManagerRef
import cross.pac.config.PacConfig
import cross.pac.json._
import cross.pac.protocol._
import cross.pac.service.{GetAdminMessages, GetArtChallenges}
import cross.routes._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

object routes extends LazyLogging {

  def pacRoutes(service: ActorRef, bot: ActorRef, thumbnailer: ActorRef, processor: ActorRef)(implicit generalConfig: GeneralConfig, pacConfig: PacConfig, system: ActorSystem, manager: SessionManagerRef, materializer: Materializer, ec: ExecutionContext): List[Route] = List(
    /** Returns the status of all project systems */
    `GET /api/pac/health` {
      implicit val tc: Timeout = Timeout.durationToTimeout(generalConfig.timeout)
      onComplete(
        List(service, bot, thumbnailer, processor)
          .map(a => (a ? GetStatus).map {
            case list: List[SystemStatus] => list
            case single: SystemStatus => List(single)
          })
          .chain(s => Future.sequence(s))
          .map(statuses => statuses.flatten)
      ) {
        case Success(statuses) =>
          complete(if (statuses.forall(s => s.healthy)) OK else ServiceUnavailable, StatusList(statuses))
        case Failure(NonFatal(up)) =>
          logger.error("failed to read actor statuses", up)
          complete(ServiceUnavailable, StatusList(SystemStatus("root", healthy = false, Some(up.getMessage)) :: Nil))
      }
    },

    /** Returns a list of all chat messages from admins */
    `GET /api/pac/admin-messages`.apply { session =>
      implicit val to: Timeout = Timeout.durationToTimeout(generalConfig.timeout)
      onSuccess(service ? GetAdminMessages) { case list: List[ChatMessage] => complete(MessageList(list)) }
    },

    /** Returns a list of all art challenges, sorted from most recent to the oldest */
    `GET /api/pac/challenges` {
      implicit val to: Timeout = Timeout.durationToTimeout(generalConfig.timeout)
      onSuccess(service ? GetArtChallenges) { case list: List[ArtChallenge] => complete(ArtChallengeList(list)) }
    }

  )

  /** Requests actor status */
  object GetStatus

  /** Describes the status of some system
    *
    * @param name    the name of the part of the project
    * @param healthy true, if the system is healthy
    * @param error   Some(message) describing why system is unhealthy
    */
  case class SystemStatus(name: String, healthy: Boolean, error: Option[String] = None)

}