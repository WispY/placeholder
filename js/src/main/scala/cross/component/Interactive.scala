package cross.component

import cross.pixi._

/** Represents a visual UI element with mouse interactions */
trait Interactive {
  var enabled = true
  var hovering = false
  var dragging = false
  var clickHandler: this.type => Unit = { _ => }
  var hoverHandler: this.type => Unit = { _ => }

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