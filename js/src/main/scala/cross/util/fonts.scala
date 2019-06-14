package cross.util

import cross.common._
import cross.component.util.Font
import cross.ffo.FontFaceObserver
import cross.util.global.GlobalContext
import cross.util.logging.Logging

import scala.concurrent.Future

object fonts extends GlobalContext with Logging {
  /** Loads given list of fonts */
  def load(fonts: List[Font]): Future[Unit] = {
    log.info(s"[fonts] loading fonts [${fonts.mkString(",")}]")
    fonts.map(load).oneByOne.clear
  }

  /** Loads given font */
  def load(font: Font): Future[Unit] = (for {
    _ <- Future.successful()
    _ = log.info(s"[fonts] loading font [$font]")
    _ <- new FontFaceObserver(font.family).load().toFuture
    _ = log.info(s"[fonts] font successfully loaded [$font]")
  } yield ()).whenFailed(up => log.error(s"[fonts] failed to load [$font]", up))
}