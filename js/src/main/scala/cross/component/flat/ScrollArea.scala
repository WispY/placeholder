package cross.component.flat

import cross.common._
import cross.component.{Component, Interactive, RedrawGraphics}
import cross.general.config.GeneralConfig
import cross.layout.{LayoutBox, StackBox}
import cross.ops._
import cross.pixi.{Container, DisplayObject}
import cross.util.spring
import cross.util.spring.DoubleSpring

/** Scrollable area */
class ScrollArea(implicit generalConfig: GeneralConfig) extends StackBox with Component with Interactive {
  private val self = this
  private var contentSize = Vec2d.Zero
  private var viewSize = Vec2d.Zero

  private val root = new Container()
  private val mask = RedrawGraphics().in(root)
  private val container = root.sub.maskWith(mask.interactive)
  private val contentLayout = new StackBox() {
    override def layoutUp(): Unit = {
      super.layoutUp()
      self.layoutUp()
    }

    override def layoutDown(box: Rec2d): Unit = {
      super.layoutDown(box)
      repositionScroll()
    }

    override def minimumSize: Vec2d = {
      val size = super.minimumSize
      contentSize = size
      size
    }
  }

  private var containerSpring = DoubleSpring(0, 0, { y => container.positionAt(0 xy y.current) }, generalConfig.scrollSpeed)
  this.init()

  override def toPixi: DisplayObject = root

  override def layoutDown(box: Rec2d): Unit = {
    super.layoutDown(box)
    mask.draw { (graphics, color) =>
      graphics.fillRect(box.size, Vec2d.Zero)
    }
    val internalBox = box.positionAt(Vec2d.Zero)
    root.positionAt(box.position)
    contentLayout.layoutDown(internalBox)
    viewSize = box.size
    repositionScroll()
  }

  override def layout(): this.type = {
    super.layout()
    contentLayout.layout()
    this
  }

  override def handleVisibility(selfVisible: Boolean, parentVisible: Boolean): Unit = {
    super.handleVisibility(selfVisible, parentVisible)
    val enabled = selfVisible && parentVisible
    if (enabled) enableWheel() else disableWheel()
  }

  /** Defines the content of this scroll area from pixi container and internal layout box */
  def content(code: (Container, LayoutBox) => Unit): this.type = {
    code.apply(container, contentLayout)
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
    onWheel { direction =>
      val delta = generalConfig.scrollDistance * (if (direction) +1 else -1)
      repositionScroll(delta)
    }
  }
}