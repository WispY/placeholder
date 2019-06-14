package cross.component.flat

import cross.component.Component
import cross.component.util.{DefaultFontStyle, FontStyle}
import cross.ops._
import cross.pixi.{DisplayObject, Text}

/** Dynamic text */
class Title(text: String = "", style: FontStyle = DefaultFontStyle) extends Component {
  private val pixiText = new Text()
  this.init()

  override def toPixi: DisplayObject = pixiText

  def setText(text: String): Unit = pixiText.text = text

  private def init(): Unit = {
    pixiText.anchorAtCenter
    pixiText.text = text
    pixiText.style = style.toTextStyle
  }
}