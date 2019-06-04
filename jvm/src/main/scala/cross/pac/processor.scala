package cross.pac

import java.util.UUID
import java.util.concurrent.Executors

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import cross.format._
import cross.mongo._
import cross.common._
import cross.pac.bot.{MessagesResponse, ReadMessages}
import cross.pac.config.PacConfig
import net.dv8tion.jda.core.entities.Message
import org.mongodb.scala.{Document, MongoClient, MongoCollection, Observable}

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import scala.util.control.NonFatal

object processor {

  /** Processes the art challenge messages */
  class ArtChallengeProcessor(bot: ActorRef, config: PacConfig) extends Actor with ActorLogging {
    private val dbPool = Executors.newFixedThreadPool(1)
    private val dbExecutor = ExecutionContext.fromExecutor(dbPool)
    private val imagePool = Executors.newFixedThreadPool(config.processor.imagePool)
    private val imageExecutor = ExecutionContext.fromExecutor(imagePool)

    private val client = MongoClient(config.processor.mongo)
    private val db = client.getDatabase(config.processor.database)

    private val submissionsFut = collection("pac.submissions")
    private val messagesFut = collection("pac.messages")
    private val challengesFur = collection("pac.challenges")

    override def preStart(): Unit = {
      self ! Startup
    }

    override def postStop(): Unit = {
      imagePool.shutdown()
    }

    override def receive: Receive = {
      case Startup =>
        implicit val ec: ExecutionContext = dbExecutor
        implicit val to: Timeout = Timeout.durationToTimeout(1.day)
        for {
          _ <- Future.successful()
          _ = log.info("starting up processor")

          _ = log.info("ensuring indices")
          submissions <- submissionsFut
          _ <- submissions.createIndex(Document("author.id" -> 1)).toFuture
          _ <- submissions.createIndex(Document("author.name" -> 1)).toFuture
          _ <- submissions.createIndex(Document("messages" -> 1)).toFuture
          _ <- submissions.createIndex(Document("timestamp" -> 1)).toFuture

          messages <- messagesFut
          _ <- messages.createIndex(Document("id" -> 1, "updateTimestamp" -> 1)).toFuture
          _ <- messages.createIndex(Document("images.id" -> 1)).toFuture

          challenges <- challengesFur
          _ <- challenges.createIndex(Document("start" -> 1)).toFuture()
          _ <- challenges.createIndex(Document("end" -> 1)).toFuture()

          _ = log.info("updating messages since last two art challenges")
          challengesTail <- challenges.find(Document()).sort(Document("start" -> -1)).limit(2).asScalaList[ArtChallenge]
          _ = log.info(s"latest art challenges: $challengesTail")
          updateTime = challengesTail.lastOption.map(c => c.start)
          _ = log.info(s"reading messages starting from [$updateTime]")
          messagesToUpdateResponse <- (bot ? ReadMessages(updateTime)).mapTo[MessagesResponse]
          _ = log.info(s"checking [${messagesToUpdateResponse.messages.size}] messages for updates")
          _ = self ! messagesToUpdateResponse.messages.chainProcessing(DoneUpdatingMessages, UpdateMessage.apply)
        } yield ()

      case UpdateMessage(message, next) =>
        implicit val ec: ExecutionContext = dbExecutor
        (for {
          messages <- messagesFut
          _ = log.info(s"updating message [${message.getId}]")
          storedMessage <- messages.find(Document("id" -> message.getId)).limit(1).asScalaOption[SubmissionMessage]
          fresh = SubmissionMessages.fromDiscord(message)
          _ <- storedMessage match {
            case None =>
              log.info(s"inserting new message [$fresh]")
              messages.insertOne(fresh.assignIds.asBson).toFuture
            case Some(old) if old.updateTimestamp == fresh.updateTimestamp =>
              log.info(s"message was not edited since last write [$old]")
              Future.successful()
            case Some(old) =>
              log.info(s"replacing outdated message with fresh one [$fresh]")
              messages.replaceOne(Document("id" -> message.getId), old.update(fresh).asBson).toFuture
          }
        } yield ()).onComplete {
          case Success(s) =>
            log.info(s"message successfully updated [${message.getId}]")
            self ! next
          case Failure(NonFatal(up)) =>
            log.error(up, s"failed to update message [${message.getId}]: $message")
        }

      case DoneUpdatingMessages =>
        log.info("done updating messages")
    }

    /** Ensures that collection exists in db */
    private def collection(name: String): Future[MongoCollection[Document]] = {
      implicit val ec: ExecutionContext = dbExecutor
      for {
        names <- db.listCollectionNames().toFuture
        _ <- if (names.contains(name)) {
          Future.successful()
        } else {
          db.createCollection(name).toFuture
        }
        ref = db.getCollection[Document](name)
      } yield ref
    }
  }

  /** Starts the pac processing */
  object Startup

  /** Requests to update the given discord message in db */
  case class UpdateMessage(message: Message, next: Any)

  /** Indicates that all messages were updated */
  object DoneUpdatingMessages

  /** Requests to create the submission image thumbnail */
  case class CreateThumbnail(submission: Submission)

  /** Describes discord user
    *
    * @param id   the discord user id
    * @param name the discord user name
    */
  case class User(id: String, name: String)

  /** Describes the user message as part of the submission
    *
    * @param id              the discord message id
    * @param createTimestamp the timestamp when message was first posted
    * @param updateTimestamp the timestamp when message was last updated
    * @param text            the cleaned up message text
    * @param images          all images attached or listed within the submission message
    */
  case class SubmissionMessage(id: String, createTimestamp: Long, updateTimestamp: Long, text: String, images: List[SubmissionImage]) {
    /** Assigns random uuids to message images */
    def assignIds: SubmissionMessage = copy(images = images.map(i => i.randomizeId))

    /** Updates the old message with fresh info */
    def update(fresh: SubmissionMessage): SubmissionMessage = {
      val imageMap = images.map(i => i.url -> i).toMap
      copy(
        updateTimestamp = fresh.updateTimestamp,
        text = fresh.text,
        images = fresh.images.map { freshImage => imageMap.getOrElse(freshImage.url, freshImage.randomizeId) }
      )
    }
  }

  object SubmissionMessages {
    /** Converts discord message into submission message */
    def fromDiscord(message: Message): SubmissionMessage = {
      val text = message.getContentRaw
      val images = message.getAttachments
        .asScala.toList
        .map { a =>
          SubmissionImage(
            id = "foo",
            url = a.getUrl,
            thumbnail = None,
            thumbnailError = false
          )
        }
      val createTimestamp = message.getCreationTime.toInstant.toEpochMilli
      val updateTimestamp = Option(message.getEditedTime).map(t => t.toInstant.toEpochMilli).getOrElse(createTimestamp)
      SubmissionMessage(
        id = message.getId,
        createTimestamp = createTimestamp,
        updateTimestamp = updateTimestamp,
        text = text,
        images = images
      )
    }
  }

  /** Describes the image submitted by user
    *
    * @param id             the generated image id
    * @param url            the image url from attachment or message
    * @param thumbnail      the url of generated thumbnail for the image
    * @param thumbnailError true, if thumbnail failed to generate
    */
  case class SubmissionImage(id: String, url: String, thumbnail: Option[String], thumbnailError: Boolean) {
    /** Assigns a new random id to the image */
    def randomizeId: SubmissionImage = copy(id = UUID.randomUUID.toString)
  }

  /** Describes the user submission
    *
    * @param author    the user who submitted the piece
    * @param messages  the list of messages forming the full submission
    * @param timestamp the time when submission was first posted
    */
  case class Submission(author: User, messages: List[SubmissionMessage], timestamp: Long)

  /** Describes the submission stored in DB */
  case class SubmissionDb(author: User, messages: List[String], timestamp: Long)

  /** Describes the art challenge period */
  case class ArtChallenge(title: String, start: Long, end: Option[Long])

  implicit val submissionImageFormat: MF[SubmissionImage] = format4(SubmissionImage)
  implicit val submissionMessageFormat: MF[SubmissionMessage] = format5(SubmissionMessage)
  implicit val artChallengeFormat: MF[ArtChallenge] = format3(ArtChallenge)

  implicit class ObservableOps(val obs: Observable[Document]) extends AnyVal {
    /** Converts a list of mongo results into scala */
    def asScalaList[A](implicit fmt: MF[A], ec: ExecutionContext): Future[List[A]] = {
      obs.toFuture().map(seq => seq.map(d => d.asScala[A]).toList)
    }

    /** Converts a list */
    def asScalaOption[A](implicit fmt: MF[A], ec: ExecutionContext): Future[Option[A]] = {
      obs.asScalaList[A].map(l => l.headOption)
    }
  }

}