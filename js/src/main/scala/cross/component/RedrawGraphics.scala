package cross.component

import cross.common.{Color, Colors, Rec2d}
import cross.ops._
import cross.pixi._

/** Represents the graphics that can change the fill color */
class RedrawGraphics(color: Color = Colors.PureBlack) extends Component {
  private val pixiContainer = new Container()
  private val pixiGraphics = new Graphics().addTo(pixiContainer)
  private var draw: (Graphics, Color) => Unit = { (g, c) => }
  private var hitbox: () => Rec2d = { () => Rec2d.Zero }
  private var fillColor = color

  /** Assigns new draw function to the graphics */
  def draw(code: (Graphics, Color) => Unit): RedrawGraphics = {
    this.draw = code
    redraw()
    this
  }

  /** Assigns new hitbox function to the graphics */
  def hitbox(code: () => Rec2d): RedrawGraphics = {
    this.hitbox = code
    redraw()
    this
  }

  /** Returns the graphics for interactions */
  def interactive: DisplayObject = pixiGraphics

  /** Updates the draw color for the graphics */
  def setColor(color: Color): RedrawGraphics = {
    this.fillColor = color
    redraw()
    this
  }

  private def redraw(): Unit = {
    pixiGraphics.clear()
    draw.apply(pixiGraphics, fillColor)
    pixiGraphics.hitArea = hitbox.apply()
  }

  override def toPixi: DisplayObject = pixiContainer
}

object RedrawGraphics {
  /** Creates new refill graphics */
  def apply(color: Color = Colors.PureBlack): RedrawGraphics = new RedrawGraphics(color)
}