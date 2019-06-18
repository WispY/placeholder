package cross.component

import cross.common
import cross.layout._
import cross.ops._
import cross.util.logging.Logging
import cross.util.mvc.GenericController

object layout {

  /** Creates the layout box that matches screen size */
  def screenLayout(implicit controller: GenericController[_]): LayoutBox = new StackBox() with Logging {
    controller.model.screen /> { case size =>
      this.size(size)
    }

    override def layoutDown(box: common.Rec2d): Unit = {
      super.layoutDown(box)
    }

    override protected def logKey: String = "screen"
  }

}