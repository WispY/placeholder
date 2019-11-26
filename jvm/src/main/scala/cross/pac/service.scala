package cross.pac

import akka.actor.{Actor, ActorLogging, Scheduler}
import akka.pattern.pipe
import cross.common._
import cross.format._
import cross.general.config.GeneralConfig
import cross.mongo._
import cross.pac.config.PacConfig
import cross.pac.processor.{ArtChallenge, SubmissionDb, SubmissionMessage}
import cross.pac.protocol._
import cross.pac.routes.{GetStatus, SystemStatus}
import org.mongodb.scala.MongoClient

import scala.concurrent.{ExecutionContext, Future}

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
      case GetStatus =>
        Future
          .sequence(submissions.status("pac.service") :: messages.status("pac.service") :: challenges.status("pac.service") :: Nil)
          .map(list => SystemStatus("pac.service", healthy = true) :: list)
          .pipeTo(sender)

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
          _ = reply ! MessageList(view)
        } yield ()

      case GetArtChallenges =>
        val reply = sender
        for {
          _ <- UnitFuture
          _ = log.info("reading all challenges")
          list <- challenges.find(
            sort = $ => $(_.start $desc)
          )
          _ = log.info(s"found [${list.size}] art challenges")
          view = list.map { challenge =>
            protocol.ArtChallenge(
              id = challenge.id,
              name = challenge.title,
              video = None,
              startTimestamp = challenge.start,
              endTimestamp = challenge.end,
              submissions = generalConfig.url(s"/api/pac/submissions?challengeId=${challenge.id}")
            )
          }
          _ = reply ! ArtChallengeList(view)
        } yield ()

      case GetSubmissions(challengeId) =>
        val reply = sender
        for {
          _ <- UnitFuture
          _ = log.info(s"looking for art challenge [$challengeId] submissions")
          challengeOpt <- challenges.findOne(
            query = $ => $(_.id $eq challengeId)
          )
          _ = log.info(s"found challenge [${challengeOpt.map(_.id)}]")
          list <- challengeOpt.map(challenge => submissions.find(
            query = $ => $(
              _.timestamp $gte challenge.start,
              _.timestamp $lte challenge.end.getOrElse(Long.MaxValue)
            ),
            sort = $ => $(_.timestamp $asc)
          )).getOrElse(Nil.future)
          _ = log.info(s"found submissions [${list.size}]")
          mappings <- Future.sequence(list.map { submission =>
            messages.find(
              query = $ => $(_.id $in submission.messages),
              sort = $ => $(_.createTimestamp $asc)
            ).map(messages => submission -> messages)
          })
          _ = log.info(s"found messages [${mappings.map(_._2.size).sum}]")
          view = mappings.map { case (submission, messageList) =>
            protocol.Submission(
              id = submission.id,
              author = submission.author.asUser(generalConfig),
              createTimestamp = submission.timestamp,
              text = messageList.map(message => message.text.trim).filterNot(text => text.isEmpty),
              resources = messageList.flatMap(message => message.images).map { image =>
                SubmissionResource(
                  id = image.id,
                  url = image.url,
                  thumbnail = image.thumbnail
                )
              }
            )
          }
          _ = reply ! SubmissionList(view)
        } yield ()
    }
  }

  /** Requests all admin messages */
  object GetAdminMessages

  /** Requests all art challenges */
  object GetArtChallenges

  /** Requests all submissions for a given art challenge */
  case class GetSubmissions(challengeId: String)

}