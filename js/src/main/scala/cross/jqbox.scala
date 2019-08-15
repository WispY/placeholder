package cross

import cross.box._
import cross.common._
import cross.util.logging.Logging
import org.querki.jquery._

object jqbox extends Logging {
  override protected def logKey: String = "jqbox"

  /** The document body */
  private val body = $("body")
  /** Registry of all current components */
  private var boxes: Map[BoxId, JQuery] = Map(BoxId.Root -> body)

  /** Creates new jq div box */
  def divBox: JQuery = $("<div>").addClass("box")

  /** Creates new jq span box */
  def spanBox: JQuery = $("<span>").addClass("box")

  implicit val boxContext: BoxContext = new BoxContext {
    /** Text metrics measurer */
    private val measurer = $("<span>").hide().appendTo(body)

    /** Creates a new component with draw functionality */
    override def drawComponent: DrawComponent = new JqDrawComponent

    /** Measures the space occupied by the text */
    override def measureText(text: String, font: Font, size: Double): Vec2d = {
      log.info(s"measuring [$text]")
      measurer
        .text(text)
        .css("font-family", font.family)
        .css("font-size", size.px)
      measurer.width() xy measurer.height()
    }

    /** Registers the box within the context */
    override def register(box: Box): Unit = {
      log.info(s"registering [$box]")
      val div = divBox
      boxes = boxes + (box.id -> div)
      box match {
        case region: RegionBox =>
          div.append(region.background.asInstanceOf[JqDrawComponent].draw)
        case text: TextBox =>
          val span = spanBox
          text.layout.style /> { case any =>
            span
              .text(text.textValue())
              .css("font-family", text.textFont().family)
              .css("font-size", text.textSize().px)
          }
          div.append(span)
        case other => // ignore
      }
      box.layout.relParents /> {
        case Nil => div.detach()
        case parent :: xs => div.appendTo(boxes(parent.id))
      }
    }

    /** Returns the very root box that matches screen size */
    override val root: Box = new ContainerBox {
      override def id: BoxId = BoxId.Root

      override def styler: Styler = Styler.Empty
    }
  }

  class JqDrawComponent extends DrawComponent {
    val draw: JQuery = divBox

    /** Clears the draw component */
    override def clear(): Unit = {
      draw.empty()
    }

    /** Fills rectangle in the given area with given color */
    override def fill(area: Rec2d, color: Color, depth: Double): Unit = {
      draw.append(
        divBox
          .css("background-color", color.toHex)
          .css("left", area.position.x.toInt)
          .css("top", area.position.y.toInt)
          .width(area.size.x)
          .height(area.size.y)
      )
    }
  }

  implicit class JqDoubleOps(val double: Double) extends AnyVal {
    /** Prints the value in pixels */
    def px: String = s"${double}px"
  }

}