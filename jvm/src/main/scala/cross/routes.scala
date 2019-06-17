package cross

import akka.http.scaladsl.server.Directives._
import cross.general.config.GeneralConfig
import cross.general.protocol._
import cross.general.session.SessionManagerRef
import cross.util.akkautil._

//noinspection TypeAnnotation
object routes {
  val `GET /api/health` = get & path("api" / "health")

  def `GET /api/config`(implicit manager: SessionManagerRef, config: GeneralConfig) = get & path("api" / "config") & adminSession()

  val `GET /api/ws` = get & path("api" / "ws")

  def `POST /api/discord`(implicit manager: SessionManagerRef) = post & path("api" / "discord") & session() & entity(as[LoginDiscord])

  def `GET /api/user`(implicit manager: SessionManagerRef) = get & path("api" / "user") & session()
}