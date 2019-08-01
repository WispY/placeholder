package cross.component.flat

import cross.common
import cross.common.{Color, Colors, Vec2d}
import cross.component.{Component, RedrawGraphics}
import cross.layout.StackBox
import cross.ops._
import cross.pixi.{Container, DisplayObject}

/** Region optionally filled with color */
class Region() extends StackBox with Component {
  private lazy val root = new Container()
  private lazy val background = RedrawGraphics(regionColor.getOrElse(Colors.PureBlack))
  private var regionColor: Option[Color] = None
  this.init()

  private def init(): Unit = {
    background.withPixi { pixi =>
      root.addChild(pixi)
      pixi.visible = regionColor.isDefined
    }
  }

  override def toPixi: DisplayObject = root

  override def layoutDown(absoluteOffset: Vec2d, box: common.Rec2d): Unit = {
    super.layoutDown(absoluteOffset, box)
    background.draw({ (graphics, color) =>
      graphics.fillRect(box.size, box.position, color)
    })
  }

  def color(color: Option[Color]): this.type = {
    regionColor = color
    background.toPixi.visible = color.isDefined
    color.foreach(c => background.setColor(c))
    this
  }

  override def handleVisibility(selfVisible: Boolean, parentVisible: Boolean): Unit = {
    root.visibleTo(selfVisible && parentVisible)
  }
}