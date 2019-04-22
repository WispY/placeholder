package cross

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

object launcher extends App {
  implicit val system: ActorSystem = ActorSystem("akka")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val execution: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = 10.seconds

//  def buildWebsocketFlow(): Flow[Message, Message, NotUsed] = {
//    val connectedWsActor = system.actorOf(WebsocketPlayer.props(manager, config), s"player_${playerCounter.getAndIncrement()}")
//
//    val incomingMessages: Sink[Message, NotUsed] = Flow[Message]
//      .map { case BinaryMessage.Strict(bytes) => IncomingMessage(text) }
//      .to(Sink.actorRef(connectedWsActor, ClientDisconnect))
//
//    val outgoingMessages: Source[Message, NotUsed] = Source
//      .actorRef[OutgoingMessage](100, OverflowStrategy.fail)
//      .mapMaterializedValue { outgoingActor =>
//        connectedWsActor ! Connected(outgoingActor)
//        NotUsed
//      }
//      .map { case OutgoingMessage(text) => TextMessage(text) }
//
//    Flow.fromSinkAndSourceCoupled(incomingMessages, outgoingMessages)
//  }
}