package cross.component

import cross.ops._
import cross.pixi._
import cross.sakura.mvc.Asset

/** Represents a simple clickable button */
class Button(assetNormal: Asset,
             assetHover: Asset,
             assetPressed: Asset,
             assetDisabled: Asset)(implicit app: Application) extends Component with Interactive {
  private val pixiContainer = new Container()
  private val pixiBackground = assetNormal.sprite.anchorAtCenter.addTo(pixiContainer)

  private val textureNormal = assetNormal.texture
  private val textureHover = assetHover.texture
  private val texturePressed = assetPressed.texture
  private val textureDisabled = assetDisabled.texture
  this.init()

  override def toPixi: DisplayObject = pixiContainer

  override def interactivePixi: DisplayObject = pixiBackground

  override def updateVisual(): Unit = {
    if (enabled) {
      if (hovering & dragging) pixiBackground.texture = texturePressed
      else if (hovering) pixiBackground.texture = textureHover
      else pixiBackground.texture = textureNormal
    } else {
      pixiBackground.texture = textureDisabled
    }
  }

  private def init(): Unit = {
    this.initInteractions()
  }
}

object Button {
  /** Creates new button */
  def apply(assetNormal: Asset,
            assetHover: Asset,
            assetPressed: Asset,
            assetDisabled: Asset)(implicit app: Application): Button = new Button(assetNormal, assetHover, assetPressed, assetDisabled)
}