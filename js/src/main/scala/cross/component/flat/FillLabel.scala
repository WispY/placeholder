package cross.component.flat

import cross.common
import cross.common._
import cross.component.Component
import cross.component.util.FontStyle
import cross.layout.StackBox
import cross.ops._
import cross.pixi.{Container, DisplayObject, Text, TextMetrics}

/** Dynamic text label that fills the given space as much as possible */
class FillLabel(style: FontStyle, maxLength: Int) extends StackBox with Component {
  private val root = new Container()
  private val text = new Text()
  private var textString = ""
  private var textWidths: List[(String, Double)] = Nil
  private var textHeight: Double = 0
  private var requiresUpdate: Boolean = false
  this.init()

  override def toPixi: DisplayObject = root

  /** Changes the text of this label to a given value */
  def label(label: String): this.type = {
    text.text = label
    textString = label
    requiresUpdate = true
    layoutUp()
    this
  }

  /** Changes the text style of this label to a given value */
  def style(style: FontStyle): this.type = {
    text.style = style.toTextStyle
    requiresUpdate = true
    layoutUp()
    this
  }

  private def recalculateWidths(): Unit = {
    if (requiresUpdate) {
      requiresUpdate = false
      textWidths = (0 to (textString.length min maxLength)).reverse.toList.map { length =>
        val string = if (length == textString.length) textString else textString.substring(0, length) + "..."
        val width = TextMetrics.measureText(string, text.style).width
        string -> width
      }
    }
  }

  override def minimumSize: common.Vec2d = {
    val size = (0 xy textHeight) + getPad * 2
    super.minimumSize maxVec size
  }

  override def layoutDown(absoluteOffset: Vec2d, box: Rec2d): Unit = {
    super.layoutDown(absoluteOffset, box)
    text.positionAt(box.position + getPad)
    recalculateWidths()
    text.text = textWidths.collectFirst { case (string, width) if width <= box.size.x => string }.getOrElse("")
  }

  private def init(): Unit = {
    root.addChild(text)
    text.style = style.toTextStyle
    text.anchorAt(Vec2d.Zero)
    textHeight = TextMetrics.measureText("A", text.style).height
    this.fillX
  }

  override def handleVisibility(selfVisible: Boolean, parentVisible: Boolean): Unit = {
    root.visibleTo(selfVisible && parentVisible)
  }
}