package cross.pac

import cross.config._
import cross.subconfig.defaults._
import cross.subconfig.formats._

/** Contains configs for Poku Art Challenge project */
object config {

  case class PacConfig(discordToken: String,
                       discordChannel: String)

  val DefaultPacConfig = PacConfig(
    discordToken = "changeme",
    discordChannel = "changeme"
  )

  implicit val reader: ConfigReader = JvmReader
  implicit val pacConfigFormat: CF[PacConfig] = configFormat2(PacConfig)

  val Config: PacConfig = null // configureNamespace("pac", Some(DefaultPacConfig))

}