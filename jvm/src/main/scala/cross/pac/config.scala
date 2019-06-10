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
    * @param retryImages          retries all failed thumbnails on startup
    */
  case class PacProcessorConfig(mongo: String,
                                database: String,
                                startupRefreshPeriod: FiniteDuration,
                                retryImages: Boolean)

  /** Configures image processor
    *
    * @param awsAccess                the access key for the aws
    * @param awsSecret                the secret key for the aws
    * @param awsBucket                the name of the s3 bucket for images
    * @param imagePool                number of thread in pool that will process images
    * @param thumbnailSize            the maximum size of the thumbnail
    * @param rateLimitRetryDelay      delay before the next upload is attempted after rate limit is hit
    * @param rateLimitSilenceDuration time before the first upload is attempted after rate limit is hit
    */
  case class PacThumbnailerConfig(awsAccess: String,
                                  awsSecret: String,
                                  awsBucket: String,
                                  imagePool: Int,
                                  thumbnailSize: Vec2i,
                                  rateLimitRetryDelay: FiniteDuration,
                                  rateLimitSilenceDuration: FiniteDuration)

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
      startupRefreshPeriod = 14.days,
      retryImages = false,
    ),
    thumbnailer = PacThumbnailerConfig(
      awsAccess = "changeme",
      awsSecret = "changeme",
      awsBucket = "art-challenge",
      imagePool = 2,
      thumbnailSize = 400 xy 1000,
      rateLimitRetryDelay = 1.hour,
      rateLimitSilenceDuration = 1.hour
    )
  )

  implicit val reader: ConfigReader = JvmReader
  implicit val vecFormat: CF[Vec2i] = format2(Vec2i.apply)
  implicit val pacBotConfigFormat: CF[PacBotConfig] = format5(PacBotConfig)
  implicit val pacProcessorConfigFormat: CF[PacProcessorConfig] = format4(PacProcessorConfig)
  implicit val pacThumbnailerConfigFormat: CF[PacThumbnailerConfig] = format7(PacThumbnailerConfig)
  implicit val pacConfigFormat: CF[PacConfig] = format3(PacConfig)

  val Config: PacConfig = configureNamespace("pac", Some(DefaultPacConfig))

}