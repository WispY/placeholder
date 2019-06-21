package cross.component.flat

import cross.common
import cross.common._
import cross.component.Component
import cross.component.util.FontStyle
import cross.layout.StackBox
import cross.ops._
import cross.pixi.{Container, DisplayObject, Text, TextMetrics}

/** Dynamic text label */
class Label(style: FontStyle) extends StackBox with Component {
  private val root = new Container()
  private val text = new Text()
  this.init()

  override def toPixi: DisplayObject = root

  /** Changes the text of this label to a given value */
  def label(label: String): this.type = {
    text.text = label
    layoutUp()
    this
  }

  /** Changes the text style of this label to a given value */
  def style(style: FontStyle): this.type = {
    text.style = style.toTextStyle
    layoutUp()
    this
  }

  override def minimumSize: common.Vec2d = {
    val metrics = TextMetrics.measureText(text.text, text.style)
    val size = (metrics.width xy metrics.height) + getPad * 2
    super.minimumSize maxVec size
  }

  override def layoutDown(absoluteOffset: Vec2d, box: Rec2d): Unit = {
    super.layoutDown(absoluteOffset, box)
    text.positionAt(box.position + getPad)
  }

  private def init(): Unit = {
    root.addChild(text)
    text.style = style.toTextStyle
    text.anchorAt(Vec2d.Zero)
    layoutUp()
  }

  override def handleVisibility(selfVisible: Boolean, parentVisible: Boolean): Unit = {
    root.visibleTo(selfVisible && parentVisible)
  }
}