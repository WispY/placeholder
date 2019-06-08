package cross.pac

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.net.URL
import java.util.Base64
import java.util.concurrent.Executors

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.model.headers.{Authorization, GenericHttpCredentials}
import akka.http.scaladsl.model.{FormData, HttpMethods, HttpRequest, StatusCodes}
import akka.http.scaladsl.unmarshalling._
import akka.stream.ActorMaterializer
import akka.util.ByteString
import cross.actors.LockActor
import cross.common._
import cross.pac.config.PacConfig
import cross.pac.processor.SubmissionImage
import net.coobird.thumbnailator.Thumbnails
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

/** Cuts thumbnails for images */
object thumbnailer {

  class Thumbnailer(materializer: ActorMaterializer, config: PacConfig) extends LockActor {
    implicit val as: ActorSystem = context.system
    implicit val m: ActorMaterializer = materializer

    private val imagePool = Executors.newFixedThreadPool(config.thumbnailer.imagePool)
    private val imageExecutor = ExecutionContext.fromExecutor(imagePool)
    private val http = Http()

    private var rateLimited = false

    override def postStop(): Unit = {
      imagePool.shutdown()
    }

    override def awaitCommands(): Receive = lock {
      case LiftRateLimit =>
        rateLimited = false
        UnitFuture

      case CreateThumbnail(image) if image.thumbnail.isDefined =>
        log.warning(s"attempted to re-create existing image thumbnail [$image]")
        sender ! ThumbnailSuccess(image, image.thumbnail.get)
        UnitFuture

      case CreateThumbnail(image) if image.thumbnailError =>
        log.warning(s"attempted to create a failed image thumbnail [$image]")
        sender ! ThumbnailError(image)
        UnitFuture

      case request@CreateThumbnail(image) =>
        val reply = sender
        if (rateLimited) {
          implicit val ec: ExecutionContext = context.system.dispatcher
          context.system.scheduler.scheduleOnce(config.thumbnailer.rateLimitRetryDelay, self, request)(ec, reply)
          UnitFuture
        } else {
          implicit val ec: ExecutionContext = imageExecutor
          implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()
          val future = for {
            _ <- UnitFuture
            _ = log.info(s"creating thumbnail for [$image]")

            _ = log.info(s"reading image from [${image.url}]")
            sourceResponse <- http.singleRequest(
              HttpRequest(method = HttpMethods.GET, uri = image.url)
            )
            _ <- if (sourceResponse.status.isSuccess()) UnitFuture else Future.failed(sys.error(s"unexpected status code [${sourceResponse.status}] during download for [${image.url}]"))
            sourceBytes <- Unmarshal(sourceResponse).to[ByteString]
            sourceStream = new ByteArrayInputStream(sourceBytes.toByteBuffer.array())

            _ = log.info(s"creating image thumbnail from [${image.url}]")
            targetBytes = new ByteArrayOutputStream()
            _ = Thumbnails
              .of(sourceStream)
              .size(config.thumbnailer.thumbnailSize.x, config.thumbnailer.thumbnailSize.y)
              .toOutputStream(targetBytes)
            encoded = Base64.getEncoder.encodeToString(targetBytes.toByteArray)

            _ = log.info(s"uploading compressed image: $encoded")
            targetResponse <- http.singleRequest(
              HttpRequest(method = HttpMethods.POST, uri = "https://api.imgur.com/3/upload")
                .withHeaders(Authorization(GenericHttpCredentials("Client-ID", config.thumbnailer.imgurClient)))
                .withEntity(FormData("image" -> encoded).toEntity)
            )
            limit = targetResponse.headers.collectFirst { case h if h.name() == "X-RateLimit-ClientRemaining" => h.value().toInt }
            _ = log.info(s"imgur is rate limiting the request to [$limit]")
            _ <- if (targetResponse.status.intValue == StatusCodes.TooManyRequests.intValue || limit.exists(l => l <= 1)) {
              log.warning("rate limiting turned on")
              rateLimited = true
              context.system.scheduler.scheduleOnce(config.thumbnailer.rateLimitSilenceDuration, self, LiftRateLimit)
              self.tell(request, reply)
              UnitFuture
            } else for {
              _ <- if (targetResponse.status.isSuccess()) UnitFuture else Future.failed(sys.error(s"unexpected status code [${targetResponse.status}] during upload for [${image.url}]"))
              imgurResponseBody <- Unmarshal(targetResponse).to[String]
              imgurResponse = imgurResponseBody.parseJson.convertTo[ImgurUploadResponse]
              targetUrl = imgurResponse.data.link
              _ = log.info(s"image successfully uploaded at [$targetUrl]")
              _ = reply ! ThumbnailSuccess(image, targetUrl)
            } yield ()
          } yield ()
          future.onComplete {
            case Success(()) => // ignore
            case Failure(NonFatal(up)) =>
              log.error(up, s"failed to process image thumbnail [$image]")
              reply ! ThumbnailError(image)
          }
          future
        }
    }
  }

  /** Requests to remove the rate limit on uploads */
  object LiftRateLimit

  /** Requests to create the submission image thumbnail */
  case class CreateThumbnail(image: SubmissionImage)

  /** Indicates that thumbnail was successfully created */
  case class ThumbnailSuccess(image: SubmissionImage, thumbnail: String)

  /** Indicates that error occurred during thumbnail processing */
  case class ThumbnailError(image: SubmissionImage)

  /** https://apidocs.imgur.com/?version=latest */
  case class ImgurUploadResponse(data: ImgurUploadData)

  /** https://apidocs.imgur.com/?version=latest */
  case class ImgurUploadData(link: String)

  implicit val imgurUploadDataJson: RootJsonFormat[ImgurUploadData] = jsonFormat1(ImgurUploadData)
  implicit val imgurUploadResponseJson: RootJsonFormat[ImgurUploadResponse] = jsonFormat1(ImgurUploadResponse)

}