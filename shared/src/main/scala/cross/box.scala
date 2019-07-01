package cross

import cross.common.{Rec2d, Vec2d}

object box {

  /** The id of the box */
  case class BoxId(value: String)

  /** Represents a 2D layout element */
  trait Box[A <: Style] {
    type Self = this.type

    /** Current layout of the box */
    private[box] var boxLayout = Layout(
      relativeChildren = Nil,
      absoluteChildren = Nil,
      parents = Nil,
      style = startingStyle,
      relativeBounds = Rec2d.Zero,
      absoluteBounds = Rec2d.Zero
    )

    /** Returns the initial style of the box */
    private[box] def startingStyle: A

    /** Returns the unique identifier of the element */
    def id: BoxId

    /** Returns the current layout of the box */
    def layout: Layout[A]

    /** Returns current style of the element */
    def style: A = layout.style

    /** Returns a list of classes that this box is assigned to */
    def classes: List[StyleClass]

    /** Returns the whole hierarchy containing this element and everything below */
    def selfAndAbsoluteChildren: List[AnyBox] = this :: layout.absoluteChildren

    /** Replaces current children with a given list of children */
    def withChildren(children: AnyBox*): Self = {
      val list = children.toList
      boxLayout.relativeChildren.foreach(child => child.withParents(boxLayout.parents :+ this))
      boxLayout = boxLayout.copy(
        relativeChildren = list,
        absoluteChildren = list.flatMap(c => c.selfAndAbsoluteChildren)
      )
      this
    }

    /** Resets the parent to None */
    private def withParents(parents: List[AnyBox]): Unit = {
      boxLayout = boxLayout.copy(parents = parents)
    }
  }

  type AnyBox = Box[_]
  type AnyLayout = Layout[_]

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
    * @param parents          a list of all parents from farthest to closest
    * @param relativeBounds   current bounds of the box within it's closest parent
    * @param absoluteBounds   current bounds of the box within it's farthest parent
    * @tparam A type of box style
    */
  case class Layout[A <: Style](style: A,
                                relativeChildren: List[AnyBox],
                                absoluteChildren: List[AnyBox],
                                parents: List[AnyBox],
                                relativeBounds: Rec2d,
                                absoluteBounds: Rec2d)

  /** Represents a style for container boxes */
  trait ContainerStyle extends Style {
    /** Returns the distance between container edge and it's children */
    def pad: Vec2d
  }

}