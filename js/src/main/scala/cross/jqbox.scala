package cross

import cross.box._
import cross.common._
import org.querki.jquery._

object jqbox {
  /** The document body */
  private val body = $("body")
  /** Registry of all current components */
  private var boxes: Map[BoxId, JQuery] = Map(BoxId.Root -> body)

  implicit val boxContext: BoxContext = new BoxContext {
    /** Text metrics measurer */
    private val measurer = $("<span>").hide().appendTo(body)

    /** Creates a new component with draw functionality */
    override def drawComponent: DrawComponent = ???

    /** Measures the space occupied by the text */
    override def measureText(text: String, font: Font, size: Double): Vec2d = ???

    /** Registers the box within the context */
    override def register(box: Box): Unit = {
      val div = $("<div>")
      boxes = boxes + (box.id -> div)
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
}