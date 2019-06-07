package cross

import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import cross.pac.processor.SubmissionImage
import cross.pac.thumbnailer.{CreateThumbnail, ThumbnailSuccess, Thumbnailer}

class ThumbnailerSpec extends AkkaSpec {
  val materializer = ActorMaterializer()
  val actor: ActorRef = system.actorOf(Props(new Thumbnailer(materializer, pac.config.Config)))

  "thumbnailer" can {
    "process image" ignore {
      val image = SubmissionImage(
        id = "foo",
        url = "http://placekitten.com/1000/600",
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