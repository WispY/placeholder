package cross.pac

import java.util.concurrent.Executors

import akka.actor.{Actor, ActorLogging, ActorRef}
import cross.pac.bot.{AdminMessages, ReadAdminMessages}
import cross.pac.config.PacConfig
import org.mongodb.scala.{Document, MongoClient, MongoCollection}

import scala.concurrent.{ExecutionContext, Future}

object processor {

  /** Processes the art challenge messages */
  class ArtChallengeProcessor(bot: ActorRef, config: PacConfig) extends Actor with ActorLogging {
    private val client = MongoClient(config.processor.mongo)
    private val db = client.getDatabase(config.processor.database)
    private val submissionsFut = collection("pac.submissions")
    private val messagesFut = collection("pac.messages")

    private val imagePool = Executors.newFixedThreadPool(config.processor.imagePool)
    private val imageExecutor = ExecutionContext.fromExecutor(imagePool)

    override def preStart(): Unit = {
      self ! Startup
    }

    override def postStop(): Unit = {
      imagePool.shutdown()
    }

    override def receive: Receive = {
      case Startup =>
        implicit val ec: ExecutionContext = context.system.dispatcher
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

          _ = log.info("reading admin messages")
          _ = bot ! ReadAdminMessages(None)
        } yield ()

      case AdminMessages(list) =>
        list.zipWithIndex.foreach { case (message, index) =>
          log.info(s"admin message [$index]: [${message.getAuthor.getName}] ${message.getContentRaw}")
        }
    }

    /** Ensures that collection exists in db */
    private def collection(name: String): Future[MongoCollection[Document]] = {
      implicit val ec: ExecutionContext = context.system.dispatcher
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
  case class SubmissionMessage(id: String, createTimestamp: Long, updateTimestamp: Long, text: String, images: List[SubmissionImage])

  /** Describes the image submitted by user
    *
    * @param id             the generated image id
    * @param url            the image url from attachment or message
    * @param thumbnail      the url of generated thumbnail for the image
    * @param thumbnailError true, if thumbnail failed to generate
    */
  case class SubmissionImage(id: String, url: String, thumbnail: Option[String], thumbnailError: Boolean)

  /** Describes the user submission
    *
    * @param author    the user who submitted the piece
    * @param messages  the list of messages forming the full submission
    * @param timestamp the time when submission was first posted
    */
  case class Submission(author: User, messages: List[SubmissionMessage], timestamp: Long)

  /** Describes the submission stored in DB */
  case class SubmissionDb(author: User, messages: List[String], timestamp: Long)

}