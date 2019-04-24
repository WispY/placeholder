package cross.component

import cross.imp._
import cross.mvc.Asset
import cross.pixi._

/** Represents a simple label with background */
class Title(asset: Asset)(implicit app: Application) extends Component {
  private val pixiContainer = new Container()
  private val pixiBackground = asset.sprite.anchorAtCenter.addTo(pixiContainer)

  override def toPixi: DisplayObject = pixiContainer
}

object Title {
  /** Creates a new title */
  def apply(asset: Asset)(implicit app: Application): Title = new Title(asset)
}