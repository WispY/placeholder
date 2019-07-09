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
    private[box] val boxLayout = Layout(self = this, style = LazyData(startingStyle))

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
    def selfAndAbsoluteChildren: List[AnyBox] = this :: layout.absChildren()

    /** Replaces current children with a given list of children */
    def withChildren(children: AnyBox*): Self = {
      val list = children.toList
      boxLayout.relChildren().foreach(child => child.updateParent(Nil))
      boxLayout.relChildren.write(list)
      list.foreach(child => child.updateParent(this :: Nil))
      this
    }

    /** Updates the layout of the box */
    def calculateLayout(): Unit

    /** Calculates the minimum width of the component */
    def calculateMinimumWidth: Double

    /** Calculates the minimum height of the component */
    def calculateMinimumHeight: Double

    /** Resets the parent to None */
    private def updateParent(parents: Boxes): Unit = {
      boxLayout.relParents.write(parents)
    }

    /** Updates the X area occupied by the box */
    private def updateAreaX(areaX: Vec2d): Unit = {
      boxLayout.relAreaX.write(areaX)
    }

    /** Updates the Y area occupied by the box */
    private def updateAreaY(areaY: Vec2d): Unit = {
      boxLayout.relAreaY.write(areaY)
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
    * @param self        reference to actual box
    * @param style       current visual style of the box
    * @param relChildren direct children of the box
    * @param absChildren all children below this box
    * @param relParents  a list of direct parents of the box
    * @param absParents  a list of all parents from farthest to closest
    * @param minW        minimum width of the box
    * @param minH        minimum height of the box
    * @param minSize     minimum size of the box
    * @param relAreaX    area X and width assigned to the box relative to parent
    * @param relAreaY    area Y and height assigned to the box relative to parent
    * @param relArea     area assigned to the box relative to parent
    * @param absAreaX    area X and width assigned to the box relative to root
    * @param absAreaY    area Y and height assigned to the box relative to root
    * @param absArea     area assigned to the box relative to root
    * @param relBoundsX  bounds X and width assigned to the box relative to parent
    * @param relBoundsY  bounds Y and height assigned to the box relative to parent
    * @param relBounds   bounds of the box relative to parent
    * @param absBoundsX  bounds X and width assigned to the box relative to root
    * @param absBoundsY  bounds Y and height assigned to the box relative to root
    * @param absBounds   bounds of the box relative to root
    * @param fill        how much free space the box should fill
    * @param align       alignment of the box bounds within assigned area
    * @param relVisible  whether the box itself is visible, invisible boxes still occupy screen space
    * @param absVisible  whether the box is visible and all parents are visible
    * @param relDisplay  whether the box itself is displayed, non-displayed boxes do not occupy screen space
    * @param absDisplay  whether the box is displayed and all parents are displayed
    * @param relEnabled  whether the box interactions are enabled
    * @param absEnabled  whether the box interactions are enabled and all parent interactions are enabled
    * @tparam A type of box style
    */
  case class Layout[A <: Style](self: Box[A],

                                style: Writeable[A],
                                relChildren: Writeable[Boxes] = LazyData(Nil),
                                absChildren: Writeable[Boxes] = LazyData(Nil),
                                relParents: Writeable[Boxes] = LazyData(Nil),
                                absParents: Writeable[Boxes] = LazyData(Nil),

                                minW: Writeable[Double] = LazyData(0),
                                minH: Writeable[Double] = LazyData(0),
                                minSize: Writeable[Vec2d] = LazyData(Vec2d.Zero),

                                relAreaX: Writeable[Vec2d] = LazyData(Vec2d.Zero),
                                relAreaY: Writeable[Vec2d] = LazyData(Vec2d.Zero),
                                relArea: Writeable[Rec2d] = LazyData(Rec2d.Zero),

                                absAreaX: Writeable[Vec2d] = LazyData(Vec2d.Zero),
                                absAreaY: Writeable[Vec2d] = LazyData(Vec2d.Zero),
                                absArea: Writeable[Rec2d] = LazyData(Rec2d.Zero),

                                relBoundsX: Writeable[Vec2d] = LazyData(Vec2d.Zero),
                                relBoundsY: Writeable[Vec2d] = LazyData(Vec2d.Zero),
                                relBounds: Writeable[Rec2d] = LazyData(Rec2d.Zero),

                                absBoundsX: Writeable[Vec2d] = LazyData(Vec2d.Zero),
                                absBoundsY: Writeable[Vec2d] = LazyData(Vec2d.Zero),
                                absBounds: Writeable[Rec2d] = LazyData(Rec2d.Zero),

                                fill: Writeable[Vec2d] = LazyData(Vec2d.Zero),
                                align: Writeable[Vec2d] = LazyData(Vec2d.Center),

                                relVisible: Writeable[Boolean] = LazyData(true),
                                absVisible: Writeable[Boolean] = LazyData(true),

                                relDisplay: Writeable[Boolean] = LazyData(true),
                                absDisplay: Writeable[Boolean] = LazyData(true),

                                relEnabled: Writeable[Boolean] = LazyData(true),
                                absEnabled: Writeable[Boolean] = LazyData(true)) {

    /** Unique subscription id for this layout */
    private implicit val listenerId: ListenerId = ListenerId(self.id.value)

    /** Calculates absolute parents and area */
    relParents />> { case (lastParents, nextParents) =>
      lastParents.foreach { parent =>
        List[Layout[_] => Writeable[_]](
          _.absParents,
          _.absAreaX,
          _.absAreaY,
          _.absVisible,
          _.absDisplay,
          _.absEnabled
        ).foreach { code => code.apply(parent.layout).forget() }
      }
      relAreaX.forget()
      relAreaY.forget()
      relVisible.forget()
      relDisplay.forget()
      relEnabled.forget()

      nextParents.foreach { parent =>
        parent.layout.absParents /> { case grandparents => absParents.write(parent :: grandparents) }
        (parent.layout.absAreaX && relAreaX) /> { case (absParent, relSelf) => absAreaX.write(relSelf.offsetX(absParent)) }
        (parent.layout.absAreaY && relAreaY) /> { case (absParent, relSelf) => absAreaY.write(relSelf.offsetY(absParent)) }
        (parent.layout.absVisible && relVisible) /> { case (absParent, relSelf) => absVisible.write(absParent && relSelf) }
        (parent.layout.absDisplay && relDisplay) /> { case (absParent, relSelf) => absDisplay.write(absParent && relSelf) }
        (parent.layout.absEnabled && relEnabled) /> { case (abdParent, relSelf) => absEnabled.write(abdParent && relSelf) }
      }
      if (nextParents.isEmpty) {
        absParents.write(Nil)
        absAreaX.write(relAreaX())
        absAreaY.write(relAreaY())
        absVisible.write(relVisible())
        absDisplay.write(relDisplay())
        absEnabled.write(relEnabled())
      }
    }

    /** Calculates absolute children */
    relChildren />> { case (lastChildren, nextChildren) =>
      lastChildren.foreach { child =>
        child.layout.absChildren.forget()
      }
      nextChildren.foreach { child =>
        child.layout.absChildren /> { case _ =>
          this.absChildren.write(relChildren() ++ relChildren().flatMap(c => c.layout.absChildren()))
        }
      }
      if (nextChildren.isEmpty) this.absChildren.write(Nil)
    }

    (minW && minH) /> { case (w, h) => minSize.write(w xy h) }
    (relAreaX && relAreaY) /> { case (x, y) => relArea.write(x coordinateRect y) }
    (absAreaX && absAreaY) /> { case (x, y) => absArea.write(x coordinateRect y) }
    (relBoundsX && relBoundsY) /> { case (x, y) => relBounds.write(x coordinateRect y) }
    (absBoundsX && absBoundsY) /> { case (x, y) => absBounds.write(x coordinateRect y) }

    /** Calculates bounds of the box */
    (fill.map(f => f.x) && align.map(a => a.x) && relAreaX && minW) /> { case (((fillX, alignX), Vec2d(areaX, areaW)), selfW) =>
      if (fillX > 0) relBoundsX.write(areaX xy areaW)
      else relBoundsX.write((areaX + alignX * (areaW - selfW)) xy selfW)
    }
    (fill.map(f => f.y) && align.map(a => a.y) && relAreaY && minH) /> { case (((fillY, alignY), Vec2d(areaY, areaH)), selfH) =>
      if (fillY > 0) relBoundsY.write(areaY xy areaH)
      else relBoundsY.write((areaY + alignY * (areaH - selfH)) xy selfH)
    }

    /** Calculates minimum size of the box */
    self match {
      case box: ChildrenSizeBox[A] =>
        absChildren />> { case _ =>
          minimumSize.write(box.childrenMinimumSize)
        }
        minimumSize /> { case size =>
          minimumWidth.write(size.x)
          minimumHeight.write(size.y)
        }
      case box: FixedSizeBox[A] =>
        val size = box.fixedMinimumSize
        minimumSize.write(size)
        minimumWidth.write(size.x)
        minimumHeight.write(size.y)
      case box: FloatingWidthBox[A] =>
        relativeBoundXW /> { case Vec2d(x, w) => minimumHeight.write(box.floatingMinimumHeight(w)) }
        (minimumWidth && minimumHeight) /> { case (w, h) => minimumSize.write(w xy h) }
    }
  }

  /** Represents a style for container boxes */
  trait ContainerStyle extends Style {
    /** Returns the distance between container edge and it's children */
    def pad: Vec2d
  }

  trait RegionStyle extends Style {
    /** Color used as a background of this region */
    def fillColor: Color
  }

  case class BasicContainerStyle(pad: Vec2d) extends ContainerStyle

}