package cross.general

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import cross.general.config._
import cross.pac.config.PacConfig
import cross.routes._
import spray.json.DefaultJsonProtocol._
import spray.json._

object routes extends SprayJsonSupport {

  /** Contains all of the projects configs */
  case class FullConfig(general: GeneralConfig = cross.general.config.Config,
                        pac: PacConfig = cross.pac.config.Config)

  implicit val fullConfigFormat: RootJsonFormat[FullConfig] = jsonFormat2(FullConfig)

  /** Returns general routes */
  def get()(implicit m: Materializer): List[Route] = List(
    `GET /api/health` {
      complete(HttpResponse(StatusCodes.OK))
    },
    `GET /api/config` {
      complete(FullConfig())
    },
    `GET /api/ws` {
      complete(HttpResponse(StatusCodes.OK))
    }
  )
}