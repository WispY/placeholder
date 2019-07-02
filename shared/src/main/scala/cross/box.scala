package cross

import cross.common._

object box {

  def container(): Box[BasicContainerStyle] = new Box[BasicContainerStyle] {
    override def startingStyle: BasicContainerStyle = BasicContainerStyle(pad = Vec2d.Zero)

    override def id: BoxId = BoxId()

    override def classes: List[StyleClass] = Nil
  }

  /** The id of the box */
  case class BoxId(value: String = uuid)

  /** Represents a 2D layout element */
  trait Box[A <: Style] {
    type Self = this.type

    /** Current layout of the box */
    private[box] val boxLayout = Layout(id = id, style = Data(startingStyle))

    /** Returns the initial style of the box */
    def startingStyle: A

    /** Returns the unique identifier of the element */
    def id: BoxId

    /** Returns the current layout of the box */
    def layout: Layout[A] = boxLayout

    /** Returns current style of the element */
    def style: A = layout.style()

    /** Returns a list of classes that this box is assigned to */
    def classes: List[StyleClass]

    /** Returns the whole hierarchy containing this element and everything below */
    def selfAndAbsoluteChildren: List[AnyBox] = this :: layout.absoluteChildren()

    /** Replaces current children with a given list of children */
    def withChildren(children: AnyBox*): Self = {
      val list = children.toList
      boxLayout.relativeChildren().foreach(child => child.withParent(Nil))
      boxLayout.relativeChildren.write(list)
      list.foreach(child => child.withParent(this :: Nil))
      this
    }

    /** Resets the parent to None */
    private def withParent(parents: Boxes): Unit = {
      boxLayout.relativeParents.write(parents)
    }
  }

  type AnyBox = Box[_]
  type AnyLayout = Layout[_]
  type Boxes = List[AnyBox]

  /** Represents a style of a 2D layout element */
  trait Style {

  }

  /** Represents a group of similarly styled boxes */
  trait StyleClass {

  }


  /** Describes the current layout of the box
    *
    * @param id               the unique identifier of the box
    * @param style            current visual style of the box
    * @param relativeChildren direct children of the box
    * @param absoluteChildren all children below this box
    * @param relativeParents  a list of direct parents of the box
    * @param absoluteParents  a list of all parents from farthest to closest
    * @param relativeBounds   current bounds of the box within it's closest parent
    * @param absoluteBounds   current bounds of the box within it's farthest parent
    * @tparam A type of box style
    */
  case class Layout[A <: Style](id: BoxId,
                                style: Writeable[A],
                                relativeChildren: Writeable[Boxes] = Data(Nil),
                                absoluteChildren: Writeable[Boxes] = Data(Nil),
                                relativeParents: Writeable[Boxes] = Data(Nil),
                                absoluteParents: Writeable[Boxes] = Data(Nil),
                                parents: Writeable[Boxes] = Data(Nil),
                                relativeBounds: Writeable[Rec2d] = Data(Rec2d.Zero),
                                absoluteBounds: Writeable[Rec2d] = Data(Rec2d.Zero)) {
    /** Unique subscription id for this layout */
    private implicit val listenerId: ListenerId = ListenerId(id.value)

    /** Calculates absolute parents */
    relativeParents />> { case (lastParents, nextParents) =>
      lastParents.foreach { parent =>
        parent.layout.absoluteParents.forget()
      }
      nextParents.foreach { parent =>
        parent.layout.absoluteParents /> { case grandparents =>
          this.absoluteParents.write(parent :: grandparents)
        }
      }
      if (nextParents.isEmpty) this.absoluteParents.write(Nil)
    }

    /** Calculates absoule children */
    relativeChildren />> { case (lastChildren, nextChildren) =>
      lastChildren.foreach { child =>
        child.layout.absoluteChildren.forget()
      }
      nextChildren.foreach { child =>
        child.layout.absoluteChildren /> { case any =>
          this.absoluteChildren.write(relativeChildren() ++ relativeChildren().flatMap(c => c.layout.absoluteChildren()))
        }
      }
      if (nextChildren.isEmpty) this.absoluteChildren.write(Nil)
    }
  }

  /** Represents a style for container boxes */
  trait ContainerStyle extends Style {
    /** Returns the distance between container edge and it's children */
    def pad: Vec2d
  }

  case class BasicContainerStyle(pad: Vec2d) extends ContainerStyle

}