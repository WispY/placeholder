package cross.component.flat

import cross.common._
import cross.component.util._
import cross.component.{Component, Interactive, RedrawGraphics}
import cross.ops._
import cross.pixi.{Container, DisplayObject}

/** Interactive button with graphics background and dynamic label */
class Button(size: Vec2d = 100 xy 50,
             depth: Double = 3,
             colorNormal: Color = Colors.BlueDark,
             colorHover: Color = Colors.Blue,
             colorPressed: Color = Colors.BlueDark,
             colorDisabled: Color = Colors.Gray,
             fontStyle: FontStyle = DefaultFontStyle,
             label: String = "") extends Component with Interactive {
  private val pixiContainer = new Container()
  private val pixiBackground = RedrawGraphics()
  private val pixiLabel = new Title()
  this.init()

  /** Updates the button size */
  def setSize(size: Vec2d): Unit = {
    pixiBackground.draw { (graphics, color) =>
      depth match {
        case 0 =>
          graphics.fillRect(size, color = color)
        case d if enabled && dragging && hovering =>
          graphics.fillRect(size.x xy d, position = 0 xy d, color = color.darker)
          graphics.fillRect(size - (0 xy (d * 2)), position = 0 xy (d * 2), color = color)
        case d =>
          graphics.fillRect(size - (0 xy d), color = color)
          graphics.fillRect(size.x xy d, position = 0 xy (size.y - d), color = color.darker)
      }
    }
    pixiBackground.hitbox(() => Rect2d(Vec2d.Zero, size))
    pixiBackground.toPixi.positionAt(size * (-0.5))
  }

  /** Updates the button label */
  def setLabel(label: String): Unit = pixiLabel.setText(label)

  override def toPixi: DisplayObject = pixiContainer

  override def interactivePixi: DisplayObject = pixiBackground.interactive

  override def updateVisual(): Unit = {
    if (enabled) {
      if (hovering && dragging) {
        pixiBackground.setColor(colorPressed)
      } else if (hovering) {
        pixiBackground.setColor(colorHover)
      } else {
        pixiBackground.setColor(colorNormal)
      }
    } else {
      pixiBackground.setColor(colorDisabled)
    }
    if (enabled && hovering && dragging) {
      // pixiLabel.toPixi.positionAt(0 xy depth)
      pixiLabel.toPixi.positionAt(0 xy (depth * 2))
    } else {
      // pixiLabel.toPixi.positionAt(0 xy (-depth))
      pixiLabel.toPixi.positionAt(0 xy 0)
    }
  }

  private def init(): Unit = {
    pixiContainer.addChild(pixiBackground.toPixi)
    pixiContainer.addChild(pixiLabel.toPixi)
    pixiBackground.setColor(colorNormal)
    setSize(size)
    setLabel(label)
    this.initInteractions()
    this.updateVisual()
  }
}