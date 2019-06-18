package cross.component.flat

import cross.common._
import cross.component.{Component, Interactive, RedrawGraphics}
import cross.layout.{LayoutBox, StackBox}
import cross.ops._
import cross.pixi.{Container, DisplayObject}
import cross.util.logging.Debug

/** Scrollable area */
class ScrollArea extends StackBox with Component with Interactive with Debug {
  private val root = new Container()
  private val hitbox = RedrawGraphics().addTo(root)
  private val mask = RedrawGraphics().addTo(root)
  private val container = root.sub.maskWith(mask.interactive)
  private val contentLayout = new StackBox()
  this.init()

  override def toPixi: DisplayObject = root

  override def layoutDown(box: Rec2d): Unit = {
    super.layoutDown(box)
    mask.draw { (graphics, color) =>
      graphics.fillRect(box.size, Vec2d.Zero)
    }
    val internalBox = box.positionAt(Vec2d.Zero)
    log.info(s"hitbox [$internalBox]")
    hitbox.hitbox(() => internalBox)
    root.positionAt(box.position)
    contentLayout.layoutDown(internalBox)
  }

  override def layout(): this.type = {
    super.layout()
    contentLayout.layout()
    this
  }

  /** Defines the content of this scroll area from pixi container and internal layout box */
  def content(code: (Container, LayoutBox) => Unit): this.type = {
    code.apply(container, contentLayout)
    this
  }

  override def interactivePixi: DisplayObject = hitbox.interactive

  override def updateVisual(): Unit = {}

  private def init(): Unit = {
    fillBoth
    onClick(_ => log.info(s"click"))
    onHover(_ => log.info(s"hover [$hovering]"))
    initInteractions()
  }
}