package cross

import akka.actor.{Actor, ActorLogging}
import cross.pac.routes.GetStatus

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

object actors {

  trait LockActor extends Actor with ActorLogging {
    private implicit val ec: ExecutionContext = context.system.dispatcher

    override def receive: Receive = Actor.emptyBehavior

    override def preStart(): Unit = {
      super.preStart()
      context.become(awaitCommands())
    }

    /** Reschedules commands for the later processing */
    private def rescheduleCommands(): Receive = {
      case Continue => context.become(awaitCommands())
      case GetStatus => awaitCommands().lift(GetStatus)
      case command => context.system.scheduler.scheduleOnce(1.second, self, command)(ec, sender)
    }

    /** Locks the processing until underlying future is complete */
    def lock(partial: PartialFunction[Any, Future[Any]]): Receive = {
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

    /** Handles the actual actor messages */
    def awaitCommands(): Receive
  }

  /** Continues processing commands */
  private object Continue

}