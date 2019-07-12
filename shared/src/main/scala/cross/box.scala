package cross

import cross.common._

object box {

  def container(): Box = new Box with ContainerStyle {
    override def id: BoxId = BoxId()

    override def classes: List[StyleClass] = Nil

    override def calculateLayoutX(): Unit = {
      layout.relChildren().foreach { child => child.updateAreaX(pad().x, child.layout.minW()) }
    }

    override def calculateLayoutY(): Unit = {
      layout.relChildren().foreach { child => child.updateAreaY(pad().y, child.layout.minH()) }
    }

    override def calculateMinimumWidth: Double = {
      val width = layout.relChildren().map(c => c.layout.minW()).maxOpt.getOrElse(0.0)
      width + pad().x * 2
    }

    override def calculateMinimumHeight: Double = {
      val height = layout.relChildren().map(c => c.layout.minH()).maxOpt.getOrElse(0.0)
      height + pad().y * 2
    }
  }

  /** The id of the box */
  case class BoxId(value: String = uuid)

  /** Represents a 2D layout element */
  trait Box {
    type Self = this.type

    /** Current layout of the box */
    private[box] val boxLayout = Layout(self = this)

    /** Returns the unique identifier of the element */
    def id: BoxId

    /** Returns the current layout of the box */
    def layout: Layout = boxLayout

    /** Returns current style of the element */
    def style: Style = layout.style()

    /** Returns a list of classes that this box is assigned to */
    def classes: List[StyleClass]

    /** Returns the whole hierarchy containing this element and everything below */
    def selfAndAbsoluteChildren: List[Box] = this :: layout.absChildren()

    /** Replaces current children with a given list of children */
    def withChildren(children: Box*): Self = {
      val list = children.toList
      boxLayout.relChildren().foreach(child => child.updateParent(Nil))
      boxLayout.relChildren.write(list)
      list.foreach(child => child.updateParent(this :: Nil))
      this
    }

    /** Updates the X layout of the box */
    def calculateLayoutX(): Unit

    /** Updates the Y layout of the box */
    def calculateLayoutY(): Unit

    /** Calculates the minimum width of the component */
    def calculateMinimumWidth: Double

    /** Calculates the minimum height of the component */
    def calculateMinimumHeight: Double

    /** Resets the parent to None */
    private def updateParent(parents: Boxes): Unit = {
      boxLayout.relParents.write(parents)
    }

    /** Updates the X area occupied by the box */
    def updateAreaX(x: Double, width: Double): Unit = {
      boxLayout.relAreaX.write(x xy width)
    }

    /** Updates the Y area occupied by the box */
    def updateAreaY(y: Double, height: Double): Unit = {
      boxLayout.relAreaY.write(y xy height)
    }
  }

  type Boxes = List[Box]
  type AnyStyleKey = StyleKey[_, _ <: Box]

  /** Refers to the box */
  case class BoxRef[A <: Box](box: A)

  /** Represents a style of a 2D layout element */
  case class Style(parameters: Map[AnyStyleKey, Any]) {
    /** Sets the style parameter */
    def set[A](key: AnyStyleKey, value: A): Style = copy(parameters = parameters + (key -> value))

    /** Returns the value assign to style parameter */
    def get[A](key: AnyStyleKey): Option[A] = parameters.get(key).map(a => a.asInstanceOf[A])
  }

  /** Refers to a single parameter for a style */
  class StyleKey[A, B <: Box](box: B, defaultValue: A) {
    /** Writes a new style value */
    def apply(newValue: A): B = {
      box.layout.style.write(box.style.set(this, newValue))
      box
    }

    /** Returns the currently set style value */
    def apply(): A = {
      box.layout.style().get[A](this).getOrElse(defaultValue)
    }

    def get: A = apply()
  }

  object StyleKey {
    /** Creates a style parameter reference */
    def apply[A, B <: Box](startingValue: A, box: B): StyleKey[A, B] = new StyleKey(box, startingValue)
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
    * @param fixedW      optional fixed width of the box
    * @param fixedH      optional fixed height of the box
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
    */
  case class Layout(self: Box,

                    style: Writeable[Style] = LazyData(Style(Map.empty)),
                    relChildren: Writeable[Boxes] = LazyData(Nil),
                    absChildren: Writeable[Boxes] = LazyData(Nil),
                    relParents: Writeable[Boxes] = LazyData(Nil),
                    absParents: Writeable[Boxes] = LazyData(Nil),

                    minW: Writeable[Double] = LazyData(0),
                    minH: Writeable[Double] = LazyData(0),
                    minSize: Writeable[Vec2d] = LazyData(Vec2d.Zero),
                    fixedW: Writeable[Option[Double]] = LazyData(None),
                    fixedH: Writeable[Option[Double]] = LazyData(None),

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

    /** List of parent data subscriptions */
    private val parentSubscriptions = List[Layout => Writeable[_]](
      _.absParents,
      _.absAreaX,
      _.absAreaY,
      _.absVisible,
      _.absDisplay,
      _.absEnabled
    )

    /** List of child data subscriptions */
    private val childSubscriptions = List[Layout => Writeable[_]](
      _.absChildren,
      _.minW,
      _.minH,
      _.relDisplay,
      _.fill
    )

    /** Calculates absolute parents and area */
    relParents />> { case (lastParents, nextParents) =>
      lastParents.foreach { parent =>
        parentSubscriptions.foreach { code => code.apply(parent.layout).forget() }
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

    /** Recalculates and writes the minimum width of the box */
    def rewriteMinW(): Unit = minW.write(self.calculateMinimumWidth max fixedW().getOrElse(0))

    /** Recalculates and writes the minimum height of the box */
    def rewriteMinH(): Unit = minH.write(self.calculateMinimumHeight max fixedH().getOrElse(0))

    /** Calculates absolute children */
    relChildren />> { case (lastChildren, nextChildren) =>
      lastChildren.foreach { child =>
        childSubscriptions.foreach { code => code.apply(child.layout).forget() }
      }
      nextChildren.foreach { child =>
        child.layout.absChildren /> { case _ =>
          absChildren.write(relChildren() ++ relChildren().flatMap(c => c.layout.absChildren()))
        }
        child.layout.minW /> { case _ => rewriteMinW() }
        child.layout.minH /> { case _ => rewriteMinH() }
        child.layout.relDisplay /> { case _ =>
          rewriteMinW()
          rewriteMinH()
        }
        child.layout.fill /> { case _ =>
          self.calculateLayoutX()
          self.calculateLayoutY()
        }
      }
      if (nextChildren.isEmpty) {
        absChildren.write(Nil)
        rewriteMinW()
        rewriteMinH()
      }
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
    (style && fixedW) /> { case _ => rewriteMinW() }
    (style && fixedH) /> { case _ => rewriteMinH() }

    /** Updates the layout of the box children */
    (style && relBoundsX && relChildren) /> { case _ => self.calculateLayoutX() }
    (style && relBoundsY && relChildren) /> { case _ => self.calculateLayoutY() }
  }

  /** Represents a style for container boxes */
  trait ContainerStyle {
    this: Box =>
    /** Space between inner components and the edge of the box */
    val pad = StyleKey(Vec2d.Zero, this)
  }

  trait RegionStyle {
    this: Box =>
    /** Color used as a background of this region */
    val fillColor = StyleKey(Colors.Black, this)
  }

}