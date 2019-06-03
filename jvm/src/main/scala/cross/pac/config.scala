package cross.pac

import cross.config._
import cross.format._

/** Contains configs for Poku Art Challenge project */
object config {

  case class PacConfig(discordToken: String,
                       discordServer: String,
                       discordChannel: String)

  val DefaultPacConfig = PacConfig(
    discordToken = "changeme",
    discordServer = "changeme",
    discordChannel = "changeme"
  )

  implicit val reader: ConfigReader = JvmReader
  implicit val pacConfigFormat: CF[PacConfig] = format3(PacConfig)

  val Config: PacConfig = configureNamespace("pac", Some(DefaultPacConfig))

}