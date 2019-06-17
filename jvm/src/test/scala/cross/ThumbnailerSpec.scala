package cross

import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import cross.pac.config.PacConfig
import cross.pac.processor.SubmissionImage
import cross.pac.thumbnailer.{CreateThumbnail, ThumbnailSuccess, Thumbnailer}

class ThumbnailerSpec extends AkkaSpec {
  implicit val config: PacConfig = pac.config.Config
  val materializer = ActorMaterializer()
  val actor: ActorRef = system.actorOf(Props(new Thumbnailer(materializer)))

  "thumbnailer" can {
    "process image" in {
      val image = SubmissionImage(
        id = "foo",
        url = "https://cdn.discordapp.com/attachments/322639465187246080/466915491080830976/mythra.jpg",
        altUrl = None,
        thumbnail = None,
        thumbnailError = false,
        marked = false
      )
      val result = (actor ? CreateThumbnail(image)).mapTo[ThumbnailSuccess].futureValue
      result.image shouldBe image
      result.thumbnail should not be empty
    }
  }
}