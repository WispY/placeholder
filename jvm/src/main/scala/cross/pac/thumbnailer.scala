package cross.pac

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.UUID
import java.util.concurrent.Executors

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.model.MediaTypes.`image/png`
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.http.scaladsl.unmarshalling._
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectMetadata
import cross.actors.LockActor
import cross.common._
import cross.pac.config.PacConfig
import cross.pac.processor.SubmissionImage
import net.coobird.thumbnailator.Thumbnails

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

/** Cuts thumbnails for images */
object thumbnailer {

  class Thumbnailer(materializer: ActorMaterializer)(implicit config: PacConfig) extends LockActor {
    implicit val as: ActorSystem = context.system
    implicit val m: ActorMaterializer = materializer

    private val s3 = AmazonS3ClientBuilder
      .standard()
      .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(config.thumbnailer.awsAccess, config.thumbnailer.awsSecret)))
      .withRegion(config.thumbnailer.awsRegion)
      .build()
    private val imagePool = Executors.newFixedThreadPool(config.thumbnailer.imagePool)
    private val imageExecutor = ExecutionContext.fromExecutor(imagePool)
    private val http = Http()
    private val imgurARegex = raw"http(s)?://imgur.com/([\w]+)".r
    private val imgurBRegex = raw"http(s)?://imgur.com/a/([\w]+)".r
    private val youtubeARegex = raw"http(s)?://www.youtube.com/watch\?v=([\w]+).*".r
    private val youtubeBRegex = raw"http(s)?://youtu.be/([\w]+).*".r

    override def postStop(): Unit = {
      imagePool.shutdown()
    }

    override def awaitCommands(): Receive = lock {
      case CreateThumbnail(image) if image.thumbnail.isDefined =>
        log.warning(s"attempted to re-create existing image thumbnail [$image]")
        sender ! ThumbnailSuccess(image, image.thumbnail.get, image.altUrl)
        UnitFuture

      case CreateThumbnail(image) =>
        implicit val ec: ExecutionContext = imageExecutor
        implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()
        val reply = sender
        val future = for {
          _ <- UnitFuture
          _ = log.info(s"creating thumbnail for [$image]")

          imageUrl = image.url
          imageAltUrl = image.url match {
            case imgurARegex(safe, id) => Some(s"http$safe://i.imgur.com/$id.png")
            case imgurBRegex(safe, id) => Some(s"http$safe://i.imgur.com/$id.png")
            case youtubeARegex(safe, id) => Some(s"http://i3.ytimg.com/vi/$id/hqdefault.jpg")
            case youtubeBRegex(safe, id) => Some(s"http://i3.ytimg.com/vi/$id/hqdefault.jpg")
            case _ => None
          }
          _ = log.info(s"reading image from [$imageUrl] alt [$imageAltUrl]")
          sourceUrl = imageAltUrl.getOrElse(imageUrl)
          sourceResponse <- http.singleRequest(
            HttpRequest(method = HttpMethods.GET, uri = sourceUrl)
          )
          _ <- if (sourceResponse.status.isSuccess()) UnitFuture else Future.failed(sys.error(s"unexpected status code [${sourceResponse.status}] during download for [$imageUrl]"))
          sourceBytes <- Unmarshal(sourceResponse.entity.withSizeLimit(Long.MaxValue)).to[ByteString]
          sourceStream = new ByteArrayInputStream(sourceBytes.toByteBuffer.array())
          targetSize = config.thumbnailer.thumbnailSize

          _ = log.info(s"creating image thumbnail from [$imageUrl]")
          targetBytesOutput = new ByteArrayOutputStream()
          _ = Thumbnails
            .of(sourceStream)
            .size(targetSize.x, targetSize.y)
            .useExifOrientation(false)
            .outputFormat("png")
            .toOutputStream(targetBytesOutput)
          targetBytesInput = new ByteArrayInputStream(targetBytesOutput.toByteArray)

          targetName = s"${UUID.randomUUID().toString}.png"
          targetUrl = s"https://${config.thumbnailer.awsBucket}.s3.amazonaws.com/$targetName"
          _ = log.info(s"uploading compressed image as [$targetName] at [$targetUrl]")
          _ = s3.putObject(config.thumbnailer.awsBucket, targetName, targetBytesInput, new ObjectMetadata().mutate(_.setContentType(`image/png`.toString)))
          _ = log.info(s"image successfully uploaded at [$targetUrl]")
          _ = reply ! ThumbnailSuccess(image, targetUrl, imageAltUrl)
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

  /** Requests to create the submission image thumbnail */
  case class CreateThumbnail(image: SubmissionImage)

  /** Indicates that thumbnail was successfully created */
  case class ThumbnailSuccess(image: SubmissionImage, thumbnail: String, altUrl: Option[String])

  /** Indicates that error occurred during thumbnail processing */
  case class ThumbnailError(image: SubmissionImage)

}