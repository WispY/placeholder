package cross.pac

import cross.config._
import cross.format._

import scala.concurrent.duration._

/** Contains configs for Poku Art Challenge project */
object config {

  /** PAC project configuration
    *
    * @param bot       the configuration for pac discord bot
    * @param processor the configuration for pac processor
    */
  case class PacConfig(bot: PacBotConfig, processor: PacProcessorConfig)

  /** Configures discord bot
    *
    * @param token               the discord token for auth
    * @param server              the name of the discord server to read
    * @param channel             the name of the discord channel to read
    * @param admins              the list of user ids to treat as admins
    * @param historyRefreshDelay the delay between refreshing message history
    */
  case class PacBotConfig(token: String,
                          server: String,
                          channel: String,
                          admins: List[String],
                          historyRefreshDelay: FiniteDuration)

  /** Configures processor
    *
    * @param mongo                the uri to connect to mongo db
    * @param database             the name of the mongo database
    * @param imagePool            number of thread in pool that will process images
    * @param startupRefreshPeriod the history duration to update when appplication starts up
    */
  case class PacProcessorConfig(mongo: String,
                                database: String,
                                imagePool: Int,
                                startupRefreshPeriod: FiniteDuration)

  val DefaultPacConfig = PacConfig(
    bot = PacBotConfig(
      token = "changeme",
      server = "kate & leo",
      channel = "bot-tests",
      admins = "337379582770675712" :: "254380888152997889" :: Nil,
      historyRefreshDelay = 1.minute
    ),
    processor = PacProcessorConfig(
      mongo = "mongodb://localhost:27017/",
      database = "pac",
      imagePool = 2,
      startupRefreshPeriod = 14.days
    )
  )

  implicit val reader: ConfigReader = JvmReader
  implicit val pacBotConfigFormat: CF[PacBotConfig] = format5(PacBotConfig)
  implicit val pacProcessorConfigFormat: CF[PacProcessorConfig] = format4(PacProcessorConfig)
  implicit val pacConfigFormat: CF[PacConfig] = format2(PacConfig)

  val Config: PacConfig = configureNamespace("pac", Some(DefaultPacConfig))

}