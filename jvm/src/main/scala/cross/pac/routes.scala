package cross.pac

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import cross.general.config.GeneralConfig
import cross.general.session.SessionManagerRef
import cross.pac.config.PacConfig
import cross.pac.protocol._
import cross.pac.service.GetAdminMessages
import cross.routes._
import cross.util.akkautil._

import scala.concurrent.ExecutionContext

object routes {

  def pacRoutes(processor: ActorRef)(implicit generalConfig: GeneralConfig, pacConfig: PacConfig, system: ActorSystem, manager: SessionManagerRef, materializer: Materializer, ec: ExecutionContext): List[Route] = List(
    /** Returns a list of chat messages from admins */
    `GET /api/pac/admin-messages`.apply { session =>
      implicit val to: Timeout = Timeout.durationToTimeout(generalConfig.timeout)
      onSuccess(processor ? GetAdminMessages) { case list: List[Message] => complete(list) }
    }
  )

}