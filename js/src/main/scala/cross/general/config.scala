package cross.general

import cross.config.{ConfigReader, _}
import cross.format.{Path, _}
import cross.util.http
import cross.util.logging.Logging

object config extends Logging {
  override protected def logKey: String = "general/config"

  /** Reads the config keys from java runtime environment */
  object JsReader extends ConfigReader {
    private val params = http.queryParameters

    override def get(path: Path): Option[String] = {
      params.get(path.stringify).flatMap(v => v.headOption)
    }
  }

  /** General configuration for all projects
    *
    * @param server         the protocol and host part for server uris
    * @param client         the protocol and host part for client uris
    * @param scrollDistance the number of pixels to jump on wheel events
    */
  case class GeneralConfig(server: String,
                           client: String,
                           discordLogin: String,
                           scrollSpeed: Double,
                           scrollDistance: Double)

  val DefaultConfig = GeneralConfig(
    server = "http://127.0.0.1:8081",
    client = http.hostPortString,
    discordLogin = s"https://discordapp.com/api/oauth2/authorize?client_id=583316882002673683&redirect_uri=${http.hostPortString}/discord&response_type=code&scope=identify",
    scrollSpeed = 0.25,
    scrollDistance = 200
  )

  implicit val generalConfigFormat: CF[GeneralConfig] = format5(GeneralConfig)

  lazy val Config: GeneralConfig = configureNamespace("general", Some(DefaultConfig))

}