package cross.util

import cross.common._
import cross.ffo.FontFaceObserver
import cross.util.global.GlobalContext
import cross.util.logging.Logging

import scala.concurrent.Future

object fonts extends GlobalContext with Logging {
  /** Loads given list of fonts */
  def load(families: List[String]): Future[Unit] = {
    log.info(s"[fonts] loading fonts [${families.mkString(",")}]")
    families.map(load).oneByOne.clear
  }

  /** Loads given font */
  def load(family: String): Future[Unit] = (for {
    _ <- Future.successful()
    _ = log.info(s"[fonts] loading font [$family]")
    _ <- new FontFaceObserver(family).load().toFuture
    _ = log.info(s"[fonts] font successfully loaded [$family]")
  } yield ()).whenFailed(up => log.error(s"[fonts] failed to load [$family]", up))
}