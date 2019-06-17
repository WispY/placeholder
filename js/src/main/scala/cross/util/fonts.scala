package cross.util

import cross.common._
import cross.component.util.Font
import cross.ffo.FontFaceObserver
import cross.util.global.GlobalContext
import cross.util.logging.Logging

import scala.concurrent.Future

object fonts extends GlobalContext with Logging {
  override protected def logKey: String = "fonts"

  /** Loads given list of fonts */
  def load(fonts: List[Font]): Future[Unit] = {
    log.info(s"loading fonts [${fonts.mkString(",")}]")
    fonts.map(load).oneByOne.clear
  }

  /** Loads given font */
  def load(font: Font): Future[Unit] = (for {
    _ <- UnitFuture
    _ = log.info(s"loading font [$font]")
    _ <- new FontFaceObserver(font.family).load().toFuture
    _ = log.info(s"font successfully loaded [$font]")
  } yield ()).whenFailed(up => log.error(s"failed to load [$font]", up))
}