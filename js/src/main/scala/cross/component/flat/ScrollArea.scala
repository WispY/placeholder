package cross.component.flat

import cross.common._
import cross.component.flat.ScrollArea.ScrollAreaStyle
import cross.component.util.Colors
import cross.component.{Component, Interactive, RedrawGraphics}
import cross.layout.{LayoutBox, StackBox, _}
import cross.ops._
import cross.pixi.{Container, DisplayObject}
import cross.util.mvc.GenericController
import cross.util.spring
import cross.util.spring.DoubleSpring

/** Scrollable area */
class ScrollArea(style: ScrollAreaStyle)(implicit controller: GenericController[_]) extends StackBox with Component with Interactive {
  private val self = this
  private var contentSize = Vec2d.Zero
  private var viewSize = Vec2d.Zero

  private val root = new Container()
  private val mask = RedrawGraphics().in(root)
  private val contentContainer = root.sub.maskWith(mask.interactive)
  private val contentLayout = new StackBox() {
    override def layoutUp(): Unit = {
      super.layoutUp()
      self.layoutUp()
    }

    override def layoutDown(absoluteOffset: Vec2d, box: Rec2d): Unit = {
      super.layoutDown(absoluteOffset, box)
      repositionScroll()
    }

    override def minimumSize: Vec2d = {
      val size = super.minimumSize
      contentSize = size
      size
    }
  }.fillBoth
  private val totalLayout = xbox.children(contentLayout, region(Colors.Red).width(style.barWidth).fillY).space(style.barSpacing)

  private val containerSpring = DoubleSpring(0, 0, { y => contentContainer.positionAt(0 xy y.current) }, style.speed)
  this.init()

  override def toPixi: DisplayObject = root

  override def layoutDown(absoluteOffset: Vec2d, box: Rec2d): Unit = {
    super.layoutDown(absoluteOffset, box)
    mask.draw { (graphics, color) =>
      graphics.fillRect(box.size, Vec2d.Zero)
    }
    val internalBox = box.positionAt(Vec2d.Zero)
    root.positionAt(box.position)
    totalLayout.layoutDown(absoluteOffset + box.position, internalBox)
    viewSize = box.size
    repositionScroll()
  }

  override def layout(): this.type = {
    super.layout()
    totalLayout.layout()
    this
  }

  override def handleVisibility(selfVisible: Boolean, parentVisible: Boolean): Unit = {
    super.handleVisibility(selfVisible, parentVisible)
    val enabled = selfVisible && parentVisible
    contentContainer.visibleTo(enabled)
    if (enabled) enableWheel() else disableWheel()
  }

  /** Defines the content of this scroll area from pixi container and internal layout box */
  def view(content: LayoutBox): this.type = {
    contentLayout.children(content.componentsIn(contentContainer))
    this
  }

  override def interactivePixi: DisplayObject = root

  override def updateVisual(): Unit = {}

  private def repositionScroll(delta: Double = 0): Unit = {
    val currentY = -containerSpring.current
    val targetY = currentY + delta
    val boundedY = (targetY max 0) min (contentSize.y - viewSize.y)
    containerSpring.target = -boundedY
  }

  private def init(): Unit = {
    this.fillBoth
    spring.add(containerSpring)
    totalLayout.componentsIn(contentContainer)
    onWheel { direction =>
      if (getAbsoluteBounds.contains(controller.model.mouse.read)) {
        val delta = style.distance * (if (direction) +1 else -1)
        repositionScroll(delta)
      }
    }
  }
}

object ScrollArea {

  /** Describes the visual style of the scroll area */
  case class ScrollAreaStyle(speed: Double,
                             distance: Double,
                             barWidth: Double,
                             barMinLength: Double,
                             barSpacing: Double)

}