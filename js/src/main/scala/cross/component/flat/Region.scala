package cross.component.flat

import cross.common
import cross.common.Vec2d
import cross.component.util.{Color, Colors}
import cross.component.{Component, RedrawGraphics}
import cross.layout.LayoutBox
import cross.ops._
import cross.pixi.{Container, DisplayObject}
import cross.util.logging.Logging

/** Region optionally filled with color */
class Region() extends Component with LayoutBox with Logging {
  private lazy val pixiContainer = new Container()
  private lazy val pixiBackground = RedrawGraphics(icolor.getOrElse(Colors.PureBlack))
  private var icolor: Option[Color] = None
  private var fixedSize: Option[Vec2d] = None
  this.init()

  private def init(): Unit = {
    pixiBackground.withPixi { pixi =>
      pixiContainer.addChild(pixi)
      pixi.visible = icolor.isDefined
    }
  }

  override def toPixi: DisplayObject = pixiContainer

  override def layoutDown(box: common.Rect2d): Unit = {
    log.info(s"layout down [$box] [$minimumSize]")
    pixiBackground.draw({ (graphics, color) =>
      graphics.fillRect(box.size, box.position, color)
    })
  }

  override def minimumSize: common.Vec2d = fixedSize.getOrElse(Vec2d.Zero)

  def color(color: Option[Color]): this.type = {
    icolor = color
    pixiBackground.toPixi.visible = color.isDefined
    color.foreach(c => pixiBackground.setColor(c))
    this
  }

  def fixedSize(size: Vec2d): this.type = {
    fixedSize = Some(size)
    layoutUp()
    this
  }

  override protected def logKey: String = "region"
}

object Region {
  /** Creates the colored region */
  def apply(container: Container): Region = new Region().withPixi(pixi => pixi.addTo(container))
}