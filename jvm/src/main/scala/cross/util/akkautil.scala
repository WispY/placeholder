package cross.util

import akka.actor.Scheduler

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

object akkautil {
  /** Attempts to execute future several times with a delay between attempts */
  def retryFuture[A](code: () => Future[A], attempts: Int, delay: FiniteDuration)(implicit ec: ExecutionContext, s: Scheduler): Future[A] = {
    code.apply().recoverWith {
      case NonFatal(up) =>
        val promise = Promise[A]()

        def rec(recAttempts: Int, lastFailure: Throwable): Unit = {
          if (recAttempts <= 0) {
            promise.failure(up)
          } else {
            delayFuture(code, delay).onComplete {
              case Success(value) => promise.success(value)
              case Failure(NonFatal(recUp)) => rec(attempts - 1, recUp)
            }
          }
        }

        rec(attempts, up)
        promise.future
    }
  }

  /** Attempts to execute code several times with a delay between attempts */
  def retryCode[A](code: () => A, attempts: Int, delay: FiniteDuration)(implicit ec: ExecutionContext, s: Scheduler): Future[A] = {
    retryFuture(() => Future(code.apply()), attempts, delay)
  }

  /** Executes the given future after the delay */
  def delayFuture[A](code: () => Future[A], delay: FiniteDuration)(implicit ec: ExecutionContext, s: Scheduler): Future[A] = {
    val promise = Promise[A]()
    s.scheduleOnce(delay, new Runnable {
      override def run(): Unit = code.apply().onComplete(result => promise.complete(result))
    })
    promise.future
  }

  /** Executes the given code after the delay */
  def delayCode[A](code: () => A, delay: FiniteDuration)(implicit ec: ExecutionContext, s: Scheduler): Future[A] = {
    delayFuture(() => Future(code.apply()), delay)
  }
}