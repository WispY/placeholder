package cross.component

import cross.layout.FixedBox
import cross.ops._
import cross.util.mvc.GenericController

object layout {

  /** Creates the layout box that matches screen size */
  def screenLayout(implicit controller: GenericController[_]): FixedBox = {
    val box = FixedBox()
    controller.model.screen /> { case size =>
      box.resizeTo(size)
    }
    box
  }

}