package cross.component

import cross.ops._
import cross.pixi._
import cross.sakura.mvc.Asset

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