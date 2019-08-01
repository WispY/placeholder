package cross.component.flat

import cross.common._
import cross.component.flat.Button.ButtonStyle
import cross.component.{Component, Interactive, RedrawGraphics}
import cross.layout.StackBox
import cross.ops._
import cross.pixi.{Container, DisplayObject}

/** Interactive button with graphics background */
class Button(style: ButtonStyle) extends StackBox with Component with Interactive {
  private val root = new Container()
  private val background = RedrawGraphics()
  this.init()

  override def toPixi: DisplayObject = root

  override def interactivePixi: DisplayObject = background.interactive

  override def updateVisual(): Unit = {
    if (enabled) {
      if (hovering && dragging) {
        background.setColor(style.colorPressed)
      } else if (hovering) {
        background.setColor(style.colorHover)
      } else {
        background.setColor(style.colorNormal)
      }
    } else {
      background.setColor(style.colorDisabled)
    }
    if (pressedVisually) {
      mapBounds(box => box.offsetBy(0 xy (style.depth * 2)))
    } else {
      mapBounds(box => box)
    }
  }

  override def layoutDown(absoluteOffset: Vec2d, box: Rec2d): Unit = {
    super.layoutDown(absoluteOffset, box)
    background.draw { (graphics, color) =>
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
    background.hitbox(() => Rec2d(Vec2d.Zero, box.size))
    background.toPixi.positionAt(box.position)
  }

  private def init(): Unit = {
    root.addChild(background.toPixi)
    this.initInteractions()
    this.updateVisual()
  }

  /** Returns true if visually button should be shifted down */
  private def pressedVisually: Boolean = enabled && hovering && dragging

  override def handleVisibility(selfVisible: Boolean, parentVisible: Boolean): Unit = {
    root.visibleTo(selfVisible && parentVisible)
  }
}

object Button {

  /** Describes the looks of a button */
  case class ButtonStyle(colorNormal: Color,
                         colorHover: Color,
                         colorPressed: Color,
                         colorDisabled: Color,
                         depth: Double)

}