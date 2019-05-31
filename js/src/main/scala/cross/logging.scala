package cross

import cross.global.GlobalContext
import org.scalajs.dom

import scala.concurrent.Future

object logging {

  /** Mix in for logging statements */
  trait Logging {
    val log: LogApi = BrowserLogApi
  }

  trait LogApi {
    /** Logs wire message */
    def wire(message: String): Unit

    /** Logs debug message */
    def debug(message: String): Unit

    /** Logs info message */
    def info(message: String): Unit

    /** Logs info message */
    def infoAsync(message: String): Future[Unit]

    /** Logs warn message */
    def warn(message: String): Unit

    /** Logs error message */
    def error(message: String, error: Throwable): Unit
  }

  /** Prints logs to browser console */
  object BrowserLogApi extends LogApi with GlobalContext {
    override def wire(message: String): Unit = if (configjs.log.Wire) {
      dom.window.console.warn(message)
    }

    override def debug(message: String): Unit = if (configjs.log.Debug) {
      dom.window.console.log(s"${System.currentTimeMillis()} $message")
    }

    override def info(message: String): Unit = if (configjs.log.Info) {
      dom.window.console.log(message)
    }

    override def infoAsync(message: String): Future[Unit] = Future {
      this.info(message)
    }

    override def warn(message: String): Unit = if (configjs.log.Warnings) {
      dom.window.console.warn(message)
    }

    override def error(message: String, error: Throwable): Unit = if (configjs.log.Errors) {
      dom.console.error(message)
      error.printStackTrace()
    }
  }

}