package cross

import akka.actor.ActorSystem
import akka.testkit.TestKit
import akka.util.Timeout

import scala.concurrent.duration._

class AkkaSpec extends TestKit(ActorSystem("test")) with Spec {
  implicit val to: Timeout = Timeout.durationToTimeout(30.seconds)

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }
}