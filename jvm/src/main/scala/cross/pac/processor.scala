package cross.pac

import java.lang.System.currentTimeMillis
import java.util.UUID

import akka.actor.{ActorRef, Scheduler}
import com.vdurmont.emoji.EmojiParser
import cross.actors.LockActor
import cross.common._
import cross.format._
import cross.general.config.GeneralConfig
import cross.general.discord.DiscordUser
import cross.mongo._
import cross.pac.bot._
import cross.pac.config.PacConfig
import cross.pac.thumbnailer.{CreateThumbnail, ThumbnailError, ThumbnailSuccess}
import net.dv8tion.jda.core.entities.Message
import org.mongodb.scala.{Document, MongoClient, Observable}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

object processor {

  /** Processes the art challenge messages */
  class ArtChallengeProcessor(bot: ActorRef, thumbnailer: ActorRef)(implicit config: PacConfig, generalConfig: GeneralConfig) extends LockActor {
    implicit val ec: ExecutionContext = context.dispatcher
    implicit val s: Scheduler = context.system.scheduler

    private val client = MongoClient(config.processor.mongo)
    private val db = client.getDatabase(config.processor.database)

    private val submissions = db.ensureCollection($$(SubmissionDb), "pac.submissions")
    private val messages = db.ensureCollection($$(SubmissionMessage), "pac.messages")
    private val challenges = db.ensureCollection($$(ArtChallenge), "pac.challenges")

    override def preStart(): Unit = {
      super.preStart()
      self ! Startup
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
            $(_.createTimestamp $asc),
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

          _ = log.info("creating missing thumbnails")
          list <- messages.find(
            query = $ => $(
              _.images.someElement.thumbnail $exists false,
              _.images.anyElement $exists true
            ),
            sort = $ => $(_.id $asc)
          )
          _ = log.info(s"found [${list.size}] messages with missing image thumbnails")
          _ = list
            .flatMap { message =>
              if (config.processor.retryImages) {
                message.images.filter(image => image.thumbnail.isEmpty && !image.marked)
              } else {
                message.images.filter(image => !image.thumbnailError && image.thumbnail.isEmpty && !image.marked)
              }
            }
            .foreach(image => thumbnailer ! CreateThumbnail(image))
        } yield ()

      case MessagePosted(message) =>
        for {
          _ <- UnitFuture
          converted = SubmissionMessages.fromDiscord(message).assignIds
          _ = log.info(s"inserting new message [$converted]")
          _ <- messages.insertOne(converted)
          _ = log.info(s"message inserted [${message.getId}]")
          _ = self ! UpdateSubmissionHavingMessage(converted.id)
          _ = converted.images.filter(image => image.thumbnail.isEmpty && !image.thumbnailError).foreach(image => thumbnailer ! CreateThumbnail(image))
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
              for {
                _ <- messages.replaceOne($ => $(_.id $eq updated.id), updated)
                _ = self ! UpdateSubmissionHavingMessage(fresh.id)
                _ = updated.images.filter(image => image.thumbnail.isEmpty && !image.thumbnailError).foreach(image => thumbnailer ! CreateThumbnail(image))
              } yield ()
          }
          _ = log.info(s"message updated [${message.getId}]")
        } yield ()

      case MessageDeleted(id) =>
        for {
          _ <- UnitFuture
          _ = log.info(s"deleting message [$id]")
          _ <- messages.deleteOne($ => $(_.id $eq id))
          _ = log.info(s"message deleted [$id]")
          _ = self ! UpdateSubmissionHavingMessage(id)
        } yield ()

      case UpdateSubmissionHavingMessage(messageId) =>
        for {
          _ <- UnitFuture
          _ = log.info(s"updating submission with message [$messageId]")
          list <- submissions.find($ => $(_.messages.anyElement $eq messageId))

          singleOpt <- if (list.size > 1) {
            val ids = list.map(sub => sub.id)
            log.info(s"found multiple submissions referring to message [$messageId], deleting all [$ids]")
            for {
              _ <- submissions.deleteMany($ => $(_.id $in ids))
            } yield None
          } else Future.successful(list.headOption)

          _ = singleOpt match {
            case Some(existing) =>
              for {
                linked <- messages.find($ => $(_.id $in existing.messages))
                _ = self ! UpdateSubmission(Submissions.fromDb(existing, linked))
              } yield ()
            case None =>
              for {
                messageOpt <- messages.findOne($ => $(_.id $eq messageId))
                _ <- messageOpt match {
                  case Some(existingMessage) =>
                    for {
                      _ <- UnitFuture
                      _ = log.info(s"searching for messages for submission from [${existingMessage.author}] around [${existingMessage.createTimestamp}]")
                      externalBefore <- messages.findOne(
                        query = $ => $(
                          _.createTimestamp $lte existingMessage.createTimestamp,
                          _.author.id $neq existingMessage.author.id
                        ),
                        sort = $ => $(_.createTimestamp $desc)
                      )
                      externalAfter <- messages.findOne(
                        query = $ => $(
                          _.createTimestamp $gte existingMessage.createTimestamp,
                          _.author.id $neq existingMessage.author.id
                        ),
                        sort = $ => $(_.createTimestamp $asc)
                      )
                      internal <- messages.find(
                        query = $ => $(
                          _.author.id $eq existingMessage.author.id,
                          _.createTimestamp $gte externalBefore.map(m => m.createTimestamp).getOrElse(-1),
                          _.createTimestamp $lte externalAfter.map(m => m.createTimestamp).getOrElse(Long.MaxValue)
                        ),
                        sort = $ => $(_.createTimestamp $asc)
                      )
                      _ = log.info(s"found submission messages [$internal]")
                      _ = if (internal.nonEmpty) {
                        self ! UpdateSubmission(Submission(
                          id = UUID.randomUUID().toString,
                          author = existingMessage.author,
                          messages = internal,
                          timestamp = internal.head.createTimestamp
                        ))
                      }
                    } yield ()
                  case None =>
                    log.info(s"did not find submission or message by id [$messageId], ignoring")
                    UnitFuture
                }
              } yield ()
          }
        } yield ()

      case UpdateSubmission(submission) =>
        for {
          _ <- UnitFuture
          _ = log.info(s"updating submission [$submission]")
          _ <- submissions.deleteMany($ => $(
            _.id $neq submission.id,
            _.messages.anyElement $in submission.messages.map(m => m.id)
          ))
          _ <- if (submission.messages.exists(message => message.images.nonEmpty)) {
            log.info(s"replacing submission with image messages [$submission]")
            for {
              _ <- UnitFuture
              count <- submissions.countDocuments($ => $(_.id $eq submission.id))
              _ <- if (count > 0) {
                submissions.replaceOne(
                  query = $ => $(_.id $eq submission.id),
                  replacement = submission.toSubmissionDb
                )
              } else {
                submissions.insertOne(submission.toSubmissionDb)
              }
            } yield ()
          } else {
            log.info(s"deleting submission without image messages [$submission]")
            submissions.deleteOne($ => $(_.id $eq submission.id))
          }
        } yield ()

      case ThumbnailSuccess(image, url, altUrl) =>
        for {
          _ <- UnitFuture
          _ = log.info(s"updating thumbnail of [$image] to [$url]")
          list <- messages.find($ => $(_.images.anyElement.id $eq image.id))
          _ = log.info(s"found [${list.size}] messages referring to uploaded thumbnail")
          futures = list.map { message =>
            val updated = message.copy(images = message.images.map {
              case uploaded if uploaded.id == image.id => uploaded.copy(thumbnail = Some(url), thumbnailError = false, altUrl = altUrl)
              case other => other
            })
            messages.replaceOne($ => $(_.id $eq message.id), updated)
          }
          _ <- Future.sequence(futures)
        } yield ()

      case ThumbnailError(image) =>
        for {
          _ <- UnitFuture
          _ = log.info(s"failing thumbnail of [$image]")
          list <- messages.find($ => $(_.images.anyElement.id $eq image.id))
          _ = log.info(s"found [${list.size}] messages referring to failed thumbnail")
          futures = list.map { message =>
            val updated = message.copy(images = message.images.map {
              case uploaded if uploaded.id == image.id => uploaded.copy(thumbnail = None, thumbnailError = true)
              case other => other
            })
            messages.replaceOne($ => $(_.id $eq message.id), updated)
          }
          _ <- Future.sequence(futures)
        } yield ()

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
          _ = reply ! list
        } yield ()
    }
  }

  /** Starts the pac processing */
  private object Startup

  /** Requests to refresh the submission data due to message changes */
  case class UpdateSubmissionHavingMessage(messageId: String)

  /** Requests to update the submission data */
  case class UpdateSubmission(submission: Submission)

  /** Requests all admin messages */
  object GetAdminMessages

  /** Describes the user message as part of the submission
    *
    * @param id              the discord message id
    * @param author          the message author
    * @param createTimestamp the timestamp when message was first posted
    * @param updateTimestamp the timestamp when message was last updated
    * @param text            the cleaned up message text
    * @param images          all images attached or listed within the submission message
    */
  case class SubmissionMessage(id: String, author: DiscordUser, createTimestamp: Long, updateTimestamp: Long, text: String, images: List[SubmissionImage]) {
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
      val (text, links) = splitText(message.getContentRaw)
      val inline = links.map { i =>
        SubmissionImage(
          id = "foo",
          url = i,
          altUrl = None,
          thumbnail = None,
          thumbnailError = false,
          marked = false
        )
      }
      val attachments = message.getAttachments
        .asScala.toList
        .map { a =>
          SubmissionImage(
            id = "foo",
            url = a.getUrl,
            altUrl = None,
            thumbnail = None,
            thumbnailError = false,
            marked = false
          )
        }
      val createTimestamp = message.getCreationTime.toInstant.toEpochMilli
      val updateTimestamp = Option(message.getEditedTime).map(t => t.toInstant.toEpochMilli).getOrElse(createTimestamp)
      SubmissionMessage(
        id = message.getId,
        author = DiscordUser(message.getAuthor.getId, message.getAuthor.getName),
        createTimestamp = createTimestamp,
        updateTimestamp = updateTimestamp,
        text = text,
        images = inline ++ attachments
      )
    }

    /** Removes emoji, extracts links */
    def splitText(text: String): (String, List[String]) = {
      val lines = text.trim.split("\n").toList
      val words = lines.map(line => line.trim.split("\\s+").toList)
      val links = words.flatten.filter(w => w.matches("http(s)?://.*"))
      val noLinks = words.map(line => line.filterNot(w => links.contains(w)))
      val noEmoji = noLinks.map { line =>
        line
          .filterNot(w => w.matches("<.*>"))
          .map(w => EmojiParser.removeAllEmojis(w))
          .filterNot(w => w.trim.isEmpty)
      }
      val clean = noEmoji.filter(line => line.nonEmpty).map(line => line.mkString(" ")).mkString("\n")
      clean -> links
    }
  }

  /** Describes the image submitted by user
    *
    * @param id             the generated image id
    * @param url            the image url from attachment or message
    * @param altUrl         the alternative url to download actual image
    * @param thumbnail      the url of generated thumbnail for the image
    * @param thumbnailError true, if thumbnail failed to generate
    * @param marked         true, if image is marked as false positive
    */
  case class SubmissionImage(id: String, url: String, altUrl: Option[String], thumbnail: Option[String], thumbnailError: Boolean, marked: Boolean) {
    /** Assigns a new random id to the image */
    def randomizeId: SubmissionImage = copy(id = UUID.randomUUID.toString)
  }

  /** Describes the user submission
    *
    * @param id        the generated id of the submission
    * @param author    the user who submitted the piece
    * @param messages  the list of messages forming the full submission
    * @param timestamp the time when submission was first posted
    */
  case class Submission(id: String, author: DiscordUser, messages: List[SubmissionMessage], timestamp: Long) {
    /** Converts to db representation */
    def toSubmissionDb: SubmissionDb = SubmissionDb(
      id = id,
      author = author,
      messages = messages.map(m => m.id),
      timestamp = timestamp
    )
  }

  object Submissions {
    /** Creates submission from stores submission and linked messages */
    def fromDb(submission: SubmissionDb, messages: List[SubmissionMessage]): Submission = Submission(
      id = submission.id,
      author = submission.author,
      messages = messages,
      timestamp = submission.timestamp
    )
  }

  /** Describes the submission stored in DB */
  case class SubmissionDb(id: String, author: DiscordUser, messages: List[String], timestamp: Long)

  /** Describes the art challenge period */
  case class ArtChallenge(title: String, start: Long, end: Option[Long])

  implicit val userFormat: MF[DiscordUser] = format2(DiscordUser)
  implicit val submissionDbFormat: MF[SubmissionDb] = format4(SubmissionDb)
  implicit val submissionImageFormat: MF[SubmissionImage] = format6(SubmissionImage)
  implicit val submissionMessageFormat: MF[SubmissionMessage] = format6(SubmissionMessage)
  implicit val artChallengeFormat: MF[ArtChallenge] = format3(ArtChallenge)

  implicit class ObservableOps(val obs: Observable[Document]) extends AnyVal {
    /** Converts a list of mongo results into scala */
    def toScalaList[A](implicit fmt: MF[A], ec: ExecutionContext): Future[List[A]] = {
      obs.toFuture().map(seq => seq.map(d => d.toScala[A]).toList)
    }

    /** Converts a list */
    def toScalaOption[A](implicit fmt: MF[A], ec: ExecutionContext): Future[Option[A]] = {
      obs.toScalaList[A].map(l => l.headOption)
    }
  }

}