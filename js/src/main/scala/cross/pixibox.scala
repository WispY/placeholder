package cross

import cross.box._
import cross.common._
import cross.ops._
import cross.pixi.{TextStyle => _, _}

object pixibox {
  /** The pixi container corresponding to box root */
  val rootContainer = new Container

  implicit val boxContext: BoxContext = new BoxContext {
    /** Creates a new component with draw functionality */
    override def drawComponent: DrawComponent = new PixiDrawComponent

    /** Measures the space occupied by the text */
    override def measureText(text: String, font: Font, size: Double): Vec2d = {
      val style = new pixi.TextStyle
      style.fontSize = size
      style.fontFamily = font.family
      val metrics = TextMetrics.measureText(text, style, wordWrap = false)
      metrics.width xy metrics.height
    }

    /** Registers the box within the context */
    override def register(box: Box): Unit = registerBox(box)

    /** Returns the very root box that matches screen size */
    override val root: Box = new ContainerBox {
      override def id: BoxId = BoxId.Root

      override def styler: Styler = Styler.Empty

      override def bind(): Unit = {
        super.bind()
        layout.absChildren /> { case children =>
          val drawables = children.flatMap(c => boxDrawables.get(c.id)).flatten
          rootContainer.children = drawables
        }
      }
    }
  }

  /** Drawing component for pixi */
  class PixiDrawComponent extends DrawComponent {
    val graphics = new Graphics()

    /** Clears the draw component */
    override def clear(): Unit = {
      graphics.clear()
    }

    /** Fills rectangle in the given area with given color */
    override def fill(area: Rec2d, color: Color, depth: Double): Unit = {
      depth match {
        case positive if positive > 0 =>
          val shadow = area.size.x xy depth
          graphics.fillRect(area.size - shadow.onlyY, area.position, color)
          graphics.fillRect(shadow, area.position + area.size.onlyY - shadow.onlyY, color.darker)
        case negative if negative < 0 =>
          val shadow = area.size.x xy depth
          graphics.fillRect(shadow, area.position + shadow.onlyY, color.darker)
          graphics.fillRect(area.size - shadow.onlyY * 2, area.position + shadow.onlyY * 2, color)
        case zero =>
          graphics.fillRect(area.size, area.position, color)
      }
    }
  }

  /** A map of drawable objects per box */
  private var boxDrawables: Map[BoxId, List[DisplayObject]] = Map.empty

  /** Extractors of pixi components from the boxes */
  private val pixiExtractors: List[PartialFunction[Box, DisplayObject]] = List(
    { case box: RegionBox =>
      val graphics = box.background.asInstanceOf[PixiDrawComponent].graphics
      box.layout.absBounds /> { case bounds => graphics.positionAt(bounds.position) }
      graphics
    },
    { case box: TextStyle =>
      val style = new pixi.TextStyle
      box.layout.style /> { case _ =>
        style.fontSize = box.textSize()
        style.fontFamily = box.textFont().family
      }
      val text = new Text(box.textValue(), style)
      box match {
        case textBox: TextBox =>
          textBox.layout.absBounds /> { case bounds => text.positionAt(bounds.position) }
        case buttonBox: ButtonBox =>
          (buttonBox.layout.styleSize && buttonBox.layout.absBounds) /> { case (_, bounds) => text.positionAt(bounds.position + buttonBox.pad()) }
      }
      text
    }
  )

  /** Registers a pixi box */
  private def registerBox(box: Box): Unit = {
    val drawables = pixiExtractors.flatMap(ex => ex.lift(box))
    boxDrawables = boxDrawables + (box.id -> drawables)
  }
}