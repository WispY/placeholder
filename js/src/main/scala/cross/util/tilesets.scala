package cross.util

import cross.box.ImageStyle.{ImageReference, ImageValue, Tileset}
import cross.common._
import cross.protocol._
import cross.util.global.GlobalContext
import cross.util.logging.Logging

import scala.concurrent.Future

object tilesets extends GlobalContext with Logging {
  override protected def logKey: String = "tilesets"

  /** Stores all loaded images */
  private var images: PartialFunction[ImageReference, ImageValue] = PartialFunction.empty

  /** Returns the value of loaded image reference */
  def value(ref: ImageReference): Option[ImageValue] = images.lift(ref)

  /** Asynchronously loads a list of tileset data */
  def load(tilesets: List[Tileset]): Future[Unit] = {
    log.info(s"loading tilesets [${tilesets.mkString(",")}]")
    tilesets.map(load).oneByOne.clear
  }

  /** Asynchronously loads tileset data */
  def load(tileset: Tileset): Future[Unit] = (for {
    _ <- UnitFuture
    _ = log.info(s"loading tileset [$tileset]")
    data <- http.resource[TilesetAreas](tileset.dataPath)
    partial = new PartialFunction[ImageReference, ImageValue] {
      override def isDefinedAt(ref: ImageReference): Boolean = tileset.images.contains(ref)

      override def apply(ref: ImageReference): ImageValue = {
        val area = data.areas(tileset.images.indexOf(ref))
        ImageValue(ref, area)
      }
    }
    _ = images = images.orElse(partial)
    _ = tileset.images.foreach(ref => images.apply(ref))
    _ = log.info(s"tileset successfully loaded [$tileset] with areas count [${data.areas.size}]")
  } yield ()).whenFailed(up => log.error(s"failed to load [$tileset]", up))
}