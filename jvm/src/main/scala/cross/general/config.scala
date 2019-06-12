package cross.general

import cross.config._
import cross.format._
import spray.json.DefaultJsonProtocol._
import spray.json._

object config {

  /** Contains configuration general for all projects
    *
    * @param host the host ip for server binding
    * @param port the server http port
    */
  case class GeneralConfig(host: String, port: Int)

  val DefaultGeneralConfig = GeneralConfig(
    host = "localhost",
    port = 8080
  )

  implicit val reader: ConfigReader = JvmReader
  implicit val generalConfigFormat: CF[GeneralConfig] = format2(GeneralConfig)

  implicit val generalConfigJsonFormat: RootJsonFormat[GeneralConfig] = jsonFormat2(GeneralConfig)

  val Config: GeneralConfig = configureNamespace("general", Some(DefaultGeneralConfig))

}