package cross

import akka.http.scaladsl.server.Directives._

//noinspection TypeAnnotation
object routes {
  val `GET /api/health` = get & path("api" / "health")
  val `GET /api/config` = get & path("api" / "config")
  val `GET /api/ws` = get & path("api" / "ws")
}
