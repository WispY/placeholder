package cross.pac

import cross.common._
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
  case class PacConfig(bot: PacBotConfig, processor: PacProcessorConfig, thumbnailer: PacThumbnailerConfig)

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
    * @param startupRefreshPeriod the history duration to update when appplication starts up
    */
  case class PacProcessorConfig(mongo: String,
                                database: String,
                                startupRefreshPeriod: FiniteDuration)

  /** Configures image processor
    *
    * @param imgurClient   the client id of the imgur application
    * @param imagePool     number of thread in pool that will process images
    * @param thumbnailSize the maximum size of the thumbnail
    */
  case class PacThumbnailerConfig(imgurClient: String,
                                  imagePool: Int,
                                  thumbnailSize: Vec2i)

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
      startupRefreshPeriod = 14.days
    ),
    thumbnailer = PacThumbnailerConfig(
      imgurClient = "changeme",
      imagePool = 2,
      thumbnailSize = 400 xy 1000
    )
  )

  implicit val reader: ConfigReader = JvmReader
  implicit val vecFormat: CF[Vec2i] = format2(Vec2i.apply)
  implicit val pacBotConfigFormat: CF[PacBotConfig] = format5(PacBotConfig)
  implicit val pacProcessorConfigFormat: CF[PacProcessorConfig] = format3(PacProcessorConfig)
  implicit val pacThumbnailerConfigFormat: CF[PacThumbnailerConfig] = format3(PacThumbnailerConfig)
  implicit val pacConfigFormat: CF[PacConfig] = format3(PacConfig)

  val Config: PacConfig = configureNamespace("pac", Some(DefaultPacConfig))

}