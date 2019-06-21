package cross.pac

import akka.actor.{Actor, ActorLogging, Scheduler}
import cross.common.UnitFuture
import cross.format.{$$, _}
import cross.general.config.GeneralConfig
import cross.mongo._
import cross.pac.config.PacConfig
import cross.pac.processor.{ArtChallenge, SubmissionDb, SubmissionMessage}
import cross.pac.protocol.ChatMessage
import org.mongodb.scala.MongoClient

import scala.concurrent.ExecutionContext

object service {

  class ArtChallengeService()(implicit config: PacConfig, generalConfig: GeneralConfig) extends Actor with ActorLogging {
    private implicit val ec: ExecutionContext = context.dispatcher
    private implicit val s: Scheduler = context.system.scheduler

    private val client = MongoClient(config.processor.mongo)
    private val db = client.getDatabase(config.processor.database)

    private val submissions = db.ensureCollection($$(SubmissionDb), "pac.submissions")
    private val messages = db.ensureCollection($$(SubmissionMessage), "pac.messages")
    private val challenges = db.ensureCollection($$(ArtChallenge), "pac.challenges")

    override def receive: Receive = {
      case GetAdminMessages =>
        val reply = sender
        for {
          _ <- UnitFuture
          admins = generalConfig.discordAdmins
          _ = log.info(s"reading all admin messages [$admins]")
          list <- messages.find(
            query = $ => $(_.author.id $in admins),
            sort = $ => $(_.createTimestamp $asc)
          )
          _ = log.info(s"found [${list.size}] admin messages")
          view = list.map { message =>
            ChatMessage(
              id = message.id,
              text = message.text,
              author = message.author.asUser,
              createTimestamp = message.createTimestamp
            )
          }
          _ = reply ! view
        } yield ()
    }
  }

  /** Requests all admin messages */
  object GetAdminMessages

}