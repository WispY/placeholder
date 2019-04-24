package cross.component

import cross.imp._
import cross.pixi._

/** Represents the graphics that can change the fill color */
class RedrawGraphics(color: Double = 0) extends Component {
  private val pixiContainer = new Container()
  private val pixiGraphics = new Graphics().addTo(pixiContainer)
  private var draw: (Graphics, Double) => Unit = { (g, c) => }
  private var fillColor = color

  /** Assigns new draw function to the graphics */
  def draw(code: (Graphics, Double) => Unit): RedrawGraphics = {
    this.draw = code
    redraw()
    this
  }

  /** Updates the draw color for the graphics */
  def setColor(color: Double): RedrawGraphics = {
    this.fillColor = color
    redraw()
    this
  }

  private def redraw(): Unit = {
    pixiGraphics.clear()
    draw.apply(pixiGraphics, fillColor)
  }

  override def toPixi: DisplayObject = pixiContainer
}

object RedrawGraphics {
  /** Creates new refill graphics */
  def apply(color: Double = 0): RedrawGraphics = new RedrawGraphics(color)
}