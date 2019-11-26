package cross.general

import cross.config._
import cross.format._
import cross.util.json._
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.concurrent.duration._

object config {

  /** Contains configuration general for all projects
    *
    * @param host            the host ip for server binding
    * @param port            the server http port
    * @param external        the base external url for the server
    * @param timeout         the general request timeout
    * @param corsOrigin      the CORS origin for the server
    * @param discordClient   the id of discord application for oauth
    * @param discordSecret   the secret of discord application for oauth
    * @param discordRedirect the uri where discord redirected oauth request
    * @param discordAdmins   the list of user ids treated as discord admins
    * @param adminApiCheck   true if the api endpoints for admins should check the role
    */
  case class GeneralConfig(host: String,
                           port: Int,
                           external: String,
                           timeout: FiniteDuration,
                           corsOrigin: String,

                           discordClient: String,
                           discordSecret: String,
                           discordRedirect: String,
                           discordAdmins: List[String],
                           adminApiCheck: Boolean) {
    /** Returns an absolute url to a given path */
    def url(path: String): String = s"$external$path"
  }

  val DefaultGeneralConfig = GeneralConfig(
    host = "localhost",
    port = 8081,
    external = "http://127.0.0.1:8081",
    timeout = 30.seconds,
    corsOrigin = "http://127.0.0.1:8080",
    discordClient = "changeme",
    discordSecret = "changeme",
    discordRedirect = "http://127.0.0.1:8080/discord",
    discordAdmins = "337379582770675712" :: "254380888152997889" :: Nil,
    adminApiCheck = true
  )

  implicit val reader: ConfigReader = JvmReader
  implicit val generalConfigFormat: CF[GeneralConfig] = format10(GeneralConfig)

  implicit val generalConfigJsonFormat: RootJsonFormat[GeneralConfig] = jsonFormat10(GeneralConfig)

  val Config: GeneralConfig = configureNamespace("general", Some(DefaultGeneralConfig))

}