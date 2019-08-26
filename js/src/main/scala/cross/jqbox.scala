package cross

import cross.box._
import cross.common._
import cross.util.logging.Logging
import cross.util.mvc.GenericController
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

  /** Listens to screen size and rescales the root */
  def scaleToScreen(controller: GenericController[_]): Unit = {
    controller.model.screen /> { case size =>
      boxContext.root.layout.fixedW.write(Some(size.x))
      boxContext.root.layout.fixedH.write(Some(size.y))
      boxes(BoxId.Root).width(size.x).height(size.y)
    }
  }

  implicit val boxContext: BoxContext = new BoxContext {
    /** Text metrics measurer */
    private val measurer = $("<span>").hide().appendTo(body)

    /** Creates a new component with draw functionality */
    override def drawComponent: DrawComponent = new JqDrawComponent

    /** Measures the space occupied by the text */
    override def measureText(text: String, font: Font, size: Double): Vec2d = {
      measurer
        .text(text)
        .css("font-family", font.family)
        .css("font-size", size.px)
      measurer.width() xy measurer.height()
    }

    /** Registers the box within the context */
    override def register(box: Box): Unit = {
      val div = divBox
        .attr("boxId", box.id.value)
      boxes = boxes + (box.id -> div)
      box match {
        case button: ButtonBox =>
          div.append(button.background.asInstanceOf[JqDrawComponent].draw)
          val span = spanBox
          button.layout.style /> { case any =>
            span
              .text(button.textValue())
              .css("font-family", button.textFont().family)
              .css("font-size", button.textSize().px)
              .css("color", button.textColor().toHex)
              .css("left", button.pad().x.px)
              .css("top", button.pad().y.px)
            div.css("cursor", button.cursor().toString.toLowerCase)
          }
          div.append(span)
          val hoverIn: EventHandler = () => button.hovering.write(true)
          val hoverOut: EventHandler = () => button.hovering.write(false)
          div.hover(hoverIn, hoverOut)
        case region: RegionBox =>
          div.append(region.background.asInstanceOf[JqDrawComponent].draw)
        case text: TextBox =>
          val span = spanBox
          text.layout.style /> { case any =>
            span
              .text(text.textValue())
              .css("font-family", text.textFont().family)
              .css("font-size", text.textSize().px)
              .css("color", text.textColor().toHex)
          }
          div.append(span)
        case other => // ignore
      }
      box.layout.relParents /> {
        case Nil => div.detach()
        case parent :: xs => div.appendTo(boxes(parent.id))
      }
      box.layout.relBounds /> {
        case bounds => div
          .css("left", bounds.position.x.px)
          .css("top", bounds.position.y.px)
          .width(bounds.size.x)
          .height(bounds.size.y)
      }
    }

    /** Returns the very root box that matches screen size */
    override val root: Box = new ContainerBox {
      override def id: BoxId = BoxId.Root

      override def styler: Styler = Styler.Empty
    }
  }

  class JqDrawComponent extends DrawComponent {
    val draw: JQuery = divBox.addClass("box-draw")

    /** Clears the draw component */
    override def clear(): Unit = {
      draw.empty()
    }

    /** Fills rectangle in the given area with given color */
    override def fill(area: Rec2d, color: Color, depth: Double): Unit = {
      draw.append(
        divBox
          .css("background-color", color.toHex)
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