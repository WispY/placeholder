package cross

import akka.http.scaladsl.server.Directives._
import cross.util.akkautil._
import cross.general.protocol._
import cross.general.session.SessionManagerRef

//noinspection TypeAnnotation
object routes {
  val `GET /api/health` = get & path("api" / "health")
  val `GET /api/config` = get & path("api" / "config")
  val `GET /api/ws` = get & path("api" / "ws")

  def `POST /api/discord`(implicit manager: SessionManagerRef) = post & path("api" / "discord") & session() & entity(as[LoginDiscord])
}