package cross.component

import cross.pixi.DisplayObject

/** Represents a visual UI element */
trait Component {
  /** Returns PIXI representation of the component */
  def toPixi: DisplayObject

  /** Executes the given code for underlying PIXI object */
  def withPixi(code: DisplayObject => Unit): this.type = {
    code.apply(toPixi)
    this
  }
}