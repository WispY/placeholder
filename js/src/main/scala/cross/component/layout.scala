package cross.component

import cross.layout._
import cross.ops._
import cross.util.mvc.GenericController

object layout {

  /** Creates the layout box that matches screen size */
  def screenLayout(implicit controller: GenericController[_]): LayoutBox = {
    val screen = box
    controller.model.screen /> { case size =>
      screen.size(size)
    }
    screen
  }

}