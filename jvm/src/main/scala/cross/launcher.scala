package cross

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import cross.config.JvmReader
import cross.pac.bot.ArtChallengeBot
import cross.pac.processor.ArtChallengeProcessor
import cross.pac.thumbnailer.Thumbnailer

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

/** Launches the server */
object launcher extends App with LazyLogging {
  config.setGlobalReader(JvmReader)

  implicit val system: ActorSystem = ActorSystem("akka")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val execution: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = 10.seconds

  val pacBot = system.actorOf(Props(new ArtChallengeBot(pac.config.Config)), "pac.bot")
  val pacThumbnailer = system.actorOf(Props(new Thumbnailer(materializer, pac.config.Config)), "pac.thumbnailer")
  val pacProcessor = system.actorOf(Props(new ArtChallengeProcessor(pacBot, pacThumbnailer, pac.config.Config)), "pac.processor")

  sys.addShutdownHook {
    system.terminate()
  }
}