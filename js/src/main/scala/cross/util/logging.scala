package cross.util

import cross.sakura.config
import cross.util.global.GlobalContext
import org.scalajs.dom

import scala.concurrent.Future

object logging {

  /** Key used as prefix during logging */
  case class LogKey(value: String) {
    override def toString: String = s"[$value]"
  }

  /** Mix in for logging statements */
  trait Logging {
    protected def logKey: String

    protected implicit val logKeyRef: LogKey = LogKey(logKey)

    protected val log: LogApi = BrowserLogApi
  }

  /** Mix in for debugging using logs */
  trait Debug extends Logging {
    override protected def logKey: String = this.getClass.getSimpleName
  }

  trait LogApi {
    /** Logs wire message */
    def wire(message: String)(implicit key: LogKey): Unit

    /** Logs debug message */
    def debug(message: String)(implicit key: LogKey): Unit

    /** Logs info message */
    def info(message: String)(implicit key: LogKey): Unit

    /** Logs info message */
    def infoAsync(message: String)(implicit key: LogKey): Future[Unit]

    /** Logs warn message */
    def warn(message: String)(implicit key: LogKey): Unit

    /** Logs error message */
    def error(message: String, error: Throwable)(implicit key: LogKey): Unit
  }

  /** Prints logs to browser console */
  object BrowserLogApi extends LogApi with GlobalContext {
    override def wire(message: String)(implicit key: LogKey): Unit = if (config.log.Wire) {
      dom.window.console.warn(s"$key $message")
    }

    override def debug(message: String)(implicit key: LogKey): Unit = if (config.log.Debug) {
      dom.window.console.log(s"$key ${System.currentTimeMillis()} $message")
    }

    override def info(message: String)(implicit key: LogKey): Unit = if (config.log.Info) {
      dom.window.console.log(s"$key $message")
    }

    override def infoAsync(message: String)(implicit key: LogKey): Future[Unit] = Future {
      this.info(message)
    }

    override def warn(message: String)(implicit key: LogKey): Unit = if (config.log.Warnings) {
      dom.window.console.warn(s"$key $message")
    }

    override def error(message: String, error: Throwable)(implicit key: LogKey): Unit = if (config.log.Errors) {
      dom.console.error(s"$key $message")
      error.printStackTrace()
    }
  }

}