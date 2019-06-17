package cross.component.flat

import cross.common
import cross.component.util.{Color, Colors}
import cross.component.{Component, RedrawGraphics}
import cross.layout.StackBox
import cross.ops._
import cross.pixi.{Container, DisplayObject}

/** Region optionally filled with color */
class Region() extends StackBox with Component {
  private lazy val pixiContainer = new Container()
  private lazy val pixiBackground = RedrawGraphics(regionColor.getOrElse(Colors.PureBlack))
  private var regionColor: Option[Color] = None
  this.init()

  private def init(): Unit = {
    pixiBackground.withPixi { pixi =>
      pixiContainer.addChild(pixi)
      pixi.visible = regionColor.isDefined
    }
  }

  override def toPixi: DisplayObject = pixiContainer

  override def layoutDown(box: common.Rec2d): Unit = {
    super.layoutDown(box)
    pixiBackground.draw({ (graphics, color) =>
      graphics.fillRect(box.size, box.position, color)
    })
  }

  def color(color: Option[Color]): this.type = {
    regionColor = color
    pixiBackground.toPixi.visible = color.isDefined
    color.foreach(c => pixiBackground.setColor(c))
    this
  }
}