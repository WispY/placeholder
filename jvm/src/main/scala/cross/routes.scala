package cross

import akka.http.scaladsl.server.Directives._
import cross.general.config.GeneralConfig
import cross.general.protocol._
import cross.general.session.SessionManagerRef
import cross.pac.json._
import cross.util.akkautil._

//noinspection TypeAnnotation
object routes {
  val `GET /api/health` = get & path("api" / "health")

  def `GET /api/config`(implicit manager: SessionManagerRef, config: GeneralConfig) = get & path("api" / "config") & adminSession()

  val `GET /api/ws` = get & path("api" / "ws")

  def `POST /api/discord`(implicit manager: SessionManagerRef) = post & path("api" / "discord") & session() & entity(as[LoginDiscord])

  def `GET /api/user`(implicit manager: SessionManagerRef) = get & path("api" / "user") & session()

  def `POST /api/signout`(implicit manager: SessionManagerRef) = post & path("api" / "signout") & session()

  val `GET /api/pac/health` = get & path("api" / "pac" / "health")

  def `GET /api/pac/admin-messages`(implicit manager: SessionManagerRef, config: GeneralConfig) = get & path("api" / "pac" / "admin-messages") & adminSession()

  val `GET /api/pac/challenges` = get & path("api" / "pac" / "challenges")

  val `GET /api/pac/submissions?challengeId={challengeId}` = get & path("api" / "pac" / "submissions") & parameter("challengeId")
}