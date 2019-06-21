package cross.component

import cross.pixi._
import cross.util.logging.Debug
import org.scalajs.dom._

import scala.ref.WeakReference

/** Represents a visual UI element with mouse interactions */
trait Interactive {
  var enabled = true
  var hovering = false
  var dragging = false
  var wheelEnabled = true
  var clickHandler: this.type => Unit = { _ => }
  var hoverHandler: this.type => Unit = { _ => }
  var wheelHandler: Boolean => Unit = { _ => }

  /** Sets the enabled to a given value */
  def setEnabled(enabled: Boolean): this.type = {
    if (enabled) enable() else disable()
  }

  /** Disables the component interactions */
  def disable(): this.type = {
    enabled = false
    interactivePixi.buttonMode = false
    updateVisual()
    this
  }

  /** Enables the component interactions */
  def enable(): this.type = {
    enabled = true
    interactivePixi.buttonMode = true
    updateVisual()
    this
  }

  /** Disables the mouse wheel events */
  def disableWheel(): this.type = {
    wheelEnabled = false
    this
  }

  /** Enables the mouse wheel events */
  def enableWheel(): this.type = {
    wheelEnabled = true
    this
  }

  /** Assigns the code that will be executed on component click */
  def onClick(code: this.type => Unit): this.type = {
    clickHandler = code
    this
  }

  /** Assigns the code that will be executed on component hover or unhover */
  def onHover(code: this.type => Unit): this.type = {
    hoverHandler = code
    hoverHandler.apply(this)
    this
  }

  /** Assigns the code that will be executed on mouse wheel events */
  def onWheel(code: Boolean => Unit): this.type = {
    val wrapped = { direction: Boolean =>
      if (wheelEnabled) code.apply(direction)
    }
    Interactive.replaceWheelHandler(wheelHandler, wrapped)
    wheelHandler = wrapped
    this
  }

  /** Removes the hover code */
  def clearHover(): this.type = {
    hoverHandler = { _ => }
    this
  }

  /** Returns the interactive part of the component */
  def interactivePixi: DisplayObject

  /** Is called every time the component is interacted with */
  def updateVisual(): Unit

  /** Initializes the component interactions */
  def initInteractions(): Unit = {
    interactivePixi.interactive = true
    interactivePixi.buttonMode = true
    interactivePixi
      .on(EventType.PointerOver, { () =>
        hovering = true
        hoverHandler.apply(this)
        updateVisual()
      })
      .on(EventType.PointerOut, { () =>
        hovering = false
        hoverHandler.apply(this)
        updateVisual()
      })
      .on(EventType.PointerDown, { () =>
        dragging = true
        updateVisual()
      })
      .on(EventType.PointerUp, { () =>
        dragging = false
        if (enabled) clickHandler.apply(this)
        updateVisual()
      })
      .on(EventType.PointerUpOutside, { () =>
        hovering = false
        dragging = false
        updateVisual()
      })
  }
}

object Interactive {
  private var handlers: List[WeakReference[Boolean => Unit]] = Nil
  this.init()

  /** Removes the old handler and adds the given code as wheel handler */
  def replaceWheelHandler(old: Boolean => Unit, code: Boolean => Unit): Unit = this.synchronized {
    handlers = handlers.filterNot(wr => wr.get.exists(h => h eq old)) :+ WeakReference(code)
  }

  /** Initializes global scroll handlers */
  private def init(): Unit = {
    document.addEventListener("wheel", handleWheelEvent, useCapture = true)
  }

  /** Executes all registered wheel handlers */
  private def handleWheelEvent(event: WheelEvent): Unit = {
    this.synchronized {
      handlers = handlers.collect {
        case ref if ref.get.isDefined =>
          ref.get.foreach(h => h.apply(event.deltaY > 0))
          ref
      }
    }
  }
}