package cross.component

import cross.common
import cross.common.Vec2d
import cross.layout._
import cross.ops._
import cross.pixi.Container
import cross.util.logging.Logging
import cross.util.mvc.GenericController

object layout {

  /** Creates the layout box that matches screen size */
  def screenLayout(container: Container)(implicit controller: GenericController[_]): LayoutBox = new StackBox() with Logging {
    controller.model.screen /> { case size =>
      this.size(size)
    }

    override def layoutDown(absoluteOffset: Vec2d, box: common.Rec2d): Unit = {
      super.layoutDown(absoluteOffset, box)
    }

    override def onChildAdded(child: LayoutBox): Unit = {
      (child :: child.getAllChildren).foreach {
        case component: Component => component.in(container)
        case _ => // ignore
      }
      super.onChildAdded(child)
    }

    override def onChildRemoved(child: LayoutBox): Unit = {
      (child :: child.getAllChildren).foreach {
        case component: Component => component.toPixi.detach
        case _ => // ignore
      }
      super.onChildRemoved(child)
    }

    override protected def logKey: String = "screen"
  }

}