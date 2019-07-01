package cross

import cross.common.{Data, Rec2d, Vec2d, Writeable}

object box {

  /** The id of the box */
  case class BoxId(value: String)

  /** Represents a 2D layout element */
  trait Box[A <: Style] {
    type Self = this.type

    /** Current layout of the box */
    private[box] val boxLayout = Layout(style = Data(startingStyle))

    /** Returns the initial style of the box */
    private[box] def startingStyle: A

    /** Returns the unique identifier of the element */
    def id: BoxId

    /** Returns the current layout of the box */
    def layout: Layout[A]

    /** Returns current style of the element */
    def style: A = layout.style()

    /** Returns a list of classes that this box is assigned to */
    def classes: List[StyleClass]

    /** Returns the whole hierarchy containing this element and everything below */
    def selfAndAbsoluteChildren: List[AnyBox] = this :: layout.absoluteChildren()

    /** Replaces current children with a given list of children */
    def withChildren(children: AnyBox*): Self = {
      val list = children.toList
      boxLayout.relativeChildren().foreach(child => child.withParent(this))
      boxLayout.relativeChildren.write(list)
      this
    }

    /** Resets the parent to None */
    private def withParent(parent: AnyBox): Unit = {
      boxLayout.relativeParents.write(parent :: Nil)
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
    * @param style            current visual style of the box
    * @param relativeChildren direct children of the box
    * @param absoluteChildren all children below this box
    * @param relativeParents  a list of direct parents of the box
    * @param absoluteParents  a list of all parents from farthest to closest
    * @param relativeBounds   current bounds of the box within it's closest parent
    * @param absoluteBounds   current bounds of the box within it's farthest parent
    * @tparam A type of box style
    */
  case class Layout[A <: Style](style: Writeable[A],
                                relativeChildren: Writeable[Boxes] = Data(Nil),
                                absoluteChildren: Writeable[Boxes] = Data(Nil),
                                relativeParents: Writeable[Boxes] = Data(Nil),
                                absoluteParents: Writeable[Boxes] = Data(Nil),
                                parents: Writeable[Boxes] = Data(Nil),
                                relativeBounds: Writeable[Rec2d] = Data(Rec2d.Zero),
                                absoluteBounds: Writeable[Rec2d] = Data(Rec2d.Zero)) {

    relativeParents /> { case parents =>}

  }

  /** Represents a style for container boxes */
  trait ContainerStyle extends Style {
    /** Returns the distance between container edge and it's children */
    def pad: Vec2d
  }

}