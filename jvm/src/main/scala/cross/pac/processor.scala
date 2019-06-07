package cross.pac

import java.lang.System.currentTimeMillis
import java.util.UUID
import java.util.concurrent.Executors

import akka.actor.{Actor, ActorLogging, ActorRef}
import cross.common._
import cross.format._
import cross.mongo._
import cross.pac.bot._
import cross.pac.config.PacConfig
import net.dv8tion.jda.core.entities.Message
import org.mongodb.scala.{Document, MongoClient, Observable}

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

object processor {

  /** Processes the art challenge messages */
  class ArtChallengeProcessor(bot: ActorRef, config: PacConfig) extends Actor with ActorLogging {
    implicit val ec: ExecutionContext = context.dispatcher

    private val imagePool = Executors.newFixedThreadPool(config.processor.imagePool)
    private val imageExecutor = ExecutionContext.fromExecutor(imagePool)

    private val client = MongoClient(config.processor.mongo)
    private val db = client.getDatabase(config.processor.database)

    private val submissions = db.ensureCollection($$(Submission), "pac.submissions")
    private val messages = db.ensureCollection($$(SubmissionMessage), "pac.messages")
    private val challenges = db.ensureCollection($$(ArtChallenge), "pac.challenges")

    override def preStart(): Unit = {
      context.become(awaitCommands())
      self ! Startup
    }

    override def postStop(): Unit = {
      imagePool.shutdown()
    }

    override def receive: Receive = Actor.emptyBehavior

    /** Reschedules commands for the later processing */
    def rescheduleCommands(): Receive = {
      case Continue => context.become(awaitCommands())
      case command => context.system.scheduler.scheduleOnce(1.second, self, command)(ec, sender)
    }

    /** Processes commands one by one */
    def awaitCommands(): Receive = lock {
      case Startup =>
        for {
          _ <- UnitFuture
          _ = log.info("starting up processor")

          _ = log.info("ensuring indices")
          _ <- submissions.ensureIndices($ => List(
            $(_.author.id $asc),
            $(_.author.name $asc),
            $(_.messages $asc),
            $(_.timestamp $asc),
          ))
          _ <- messages.ensureIndices($ => List(
            $(_.id $asc, _.updateTimestamp $asc),
            $(_.images.anyElement.id $asc)
          ))
          _ <- challenges.ensureIndices($ => List(
            $(_.start $asc),
            $(_.end $asc)
          ))

          _ = log.info("checking if any messages are stored in db")
          count <- messages.countDocuments()
          updateTimeOpt = if (count == 0L) None else Some(currentTimeMillis - config.processor.startupRefreshPeriod.toMillis)
          _ = log.info(s"reading messages state starting from [$updateTimeOpt]")
          history <- updateTimeOpt
            .map(updateTime => messages.find($ => $(_.createTimestamp $gt updateTime)))
            .getOrElse(messages.find())
          timestamps = history.map(message => message.id -> message.updateTimestamp).toMap
          _ = log.info(s"updating messages starting from [$updateTimeOpt]")
          _ = bot ! UpdateHistory(updateTimeOpt, timestamps)
        } yield ()

      case MessagePosted(message) =>
        for {
          _ <- UnitFuture
          converted = SubmissionMessages.fromDiscord(message).assignIds
          _ = log.info(s"inserting new message [$converted]")
          _ <- messages.insertOne(converted)
          _ = log.info(s"message inserted [${message.getId}]")
        } yield ()

      case MessageEdited(message) =>
        for {
          _ <- UnitFuture
          _ = log.info(s"updating message [${message.getId}]")
          storedMessage <- messages.findOne($ => $(_.id $eq message.getId))
          fresh = SubmissionMessages.fromDiscord(message)
          _ <- storedMessage match {
            case None =>
              log.warning(s"missing message that was edited [$fresh], rescheduling as new message")
              self ! MessagePosted(message)
              UnitFuture
            case Some(old) if old.updateTimestamp == fresh.updateTimestamp =>
              log.info(s"message was not edited since last write [$old]")
              UnitFuture
            case Some(old) =>
              val updated = old.update(fresh)
              log.info(s"replacing outdated message with fresh one [$updated]")
              messages.replaceOne($ => $(_.id $eq updated.id), updated)
          }
          _ = log.info(s"message updated [${message.getId}]")
        } yield ()

      case MessageDeleted(id) =>
        for {
          _ <- UnitFuture
          _ = log.info(s"deleting message [$id]")
          _ <- messages.deleteOne($ => $(_.id $eq id))
          _ = log.info(s"message deleted [$id]")
        } yield ()
    }

    /** Locks the processing until underlying future is complete */
    private def lock(partial: PartialFunction[Any, Future[Any]]): Receive = {
      case command if partial.isDefinedAt(command) =>
        context.become(rescheduleCommands())
        partial.apply(command).onComplete {
          case Success(a) =>
            self ! Continue
          case Failure(NonFatal(up)) =>
            log.error(up, s"failed to process command [$command], continuing to next one")
            self ! Continue
        }
      case undefined =>
        log.warning(s"unknown command [$undefined], it will be skipped")
    }
  }

  /** Starts the pac processing */
  private object Startup

  /** Continues processing commands */
  private object Continue

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
    * @param author          the message author
    * @param createTimestamp the timestamp when message was first posted
    * @param updateTimestamp the timestamp when message was last updated
    * @param text            the cleaned up message text
    * @param images          all images attached or listed within the submission message
    */
  case class SubmissionMessage(id: String, author: User, createTimestamp: Long, updateTimestamp: Long, text: String, images: List[SubmissionImage]) {
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
        author = User(message.getAuthor.getId, message.getAuthor.getName),
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

  implicit val userFormat: MF[User] = format2(User)
  implicit val submissionFormat: MF[Submission] = format3(Submission)
  implicit val submissionImageFormat: MF[SubmissionImage] = format4(SubmissionImage)
  implicit val submissionMessageFormat: MF[SubmissionMessage] = format6(SubmissionMessage)
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