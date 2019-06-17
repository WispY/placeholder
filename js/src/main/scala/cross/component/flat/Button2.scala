package cross.component.flat

import cross.common._
import cross.component.flat.Button.ButtonStyle
import cross.component.{Component, Interactive, RedrawGraphics}
import cross.layout.StackBox
import cross.ops._
import cross.pixi.{Container, DisplayObject}

/** Interactive button with graphics background */
class Button2(style: ButtonStyle) extends StackBox with Component with Interactive {
  private val pixiContainer = new Container()
  private val pixiBackground = RedrawGraphics()
  this.init()

  override def toPixi: DisplayObject = pixiContainer

  override def interactivePixi: DisplayObject = pixiBackground.interactive

  override def updateVisual(): Unit = {
    if (enabled) {
      if (hovering && dragging) {
        pixiBackground.setColor(style.colorPressed)
      } else if (hovering) {
        pixiBackground.setColor(style.colorHover)
      } else {
        pixiBackground.setColor(style.colorNormal)
      }
    } else {
      pixiBackground.setColor(style.colorDisabled)
    }
    if (pressedVisually) {
      mapBounds(box => box.offsetBy(0 xy (style.depth * 2)))
    } else {
      mapBounds(box => box)
    }
  }

  override def layoutDown(box: Rec2d): Unit = {
    super.layoutDown(box)
    pixiBackground.draw { (graphics, color) =>
      style.depth match {
        case 0 =>
          graphics.fillRect(box.size, Vec2d.Zero, color)
        case d if pressedVisually =>
          graphics.fillRect(box.size.x xy d, 0 xy -d, color.darker)
          graphics.fillRect(box.size - (0 xy (d * 2)), Vec2d.Zero, color)
        case d =>
          graphics.fillRect(box.size - (0 xy d), Vec2d.Zero, color)
          graphics.fillRect(box.size.x xy d, 0 xy (box.size.y - d), color.darker)
      }
    }
    pixiBackground.hitbox(() => Rec2d(Vec2d.Zero, box.size))
    pixiBackground.toPixi.positionAt(box.position)
  }


  private def init(): Unit = {
    pixiContainer.addChild(pixiBackground.toPixi)
    this.initInteractions()
    this.updateVisual()
  }

  /** Returns true if visually button should be shifted down */
  private def pressedVisually: Boolean = enabled && hovering && dragging
}