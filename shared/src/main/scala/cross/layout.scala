package cross

import cross.common._

object layout {

  /** Represents a 2D object with layout properties */
  trait LayoutBox {
    private[layout] var fill: Vec2i = Vec2i.Zero
    private[layout] var children: List[LayoutBox] = Nil
    private[layout] var parent: Option[LayoutBox] = None
    private[layout] var ialign: Vec2d = Vec2d.Center

    /** Adds the child to this box */
    def add(child: LayoutBox): this.type = this.synchronized {
      children = children :+ child
      child.parent = Some(this)
      layoutUp()
      this
    }

    /** Removes the child from this box */
    def remove(child: LayoutBox): this.type = this.synchronized {
      children = children.filterNot(c => c eq child)
      child.parent = None
      layoutUp()
      this
    }

    /** Removes this box from the parent */
    def detach: this.type = this.synchronized {
      parent.foreach(p => p.remove(this))
      layoutUp()
      this
    }

    /** Propagates the layout request up the parent chain */
    def layoutUp(): Unit = parent match {
      case Some(p) => p.layoutUp()
      case None => layoutDown(Rect2d(Vec2d.Zero, minimumSize))
    }

    /** Updates the layout of the box within given bounds */
    def layoutDown(box: Rect2d): Unit

    /** Aligns the box within the given bounds */
    def alignWithin(box: Rect2d, forcedSize: Option[Vec2d] = None): Rect2d = {
      val minSize = minimumSize
      val targetSize = forcedSize.getOrElse((if (fill.x == 1) box.size.x else minSize.x) xy (if (fill.y == 1) box.size.y else minSize.y))
      val targetOffset = ialign * (box.size - targetSize)
      box.offsetBy(targetOffset).resizeTo(targetSize)
    }

    /** Returns the minimum dimensions of this box */
    def minimumSize: Vec2d

    /** Sets the box to take all available space at X coordinate */
    def fillX: this.type = {
      fill = 1 xy fill.y
      layoutUp()
      this
    }

    /** Sets the box to take all available space at Y coordinate */
    def fillY: this.type = {
      fill = fill.x xy 1
      layoutUp()
      this
    }

    /** Sets the box to take all available space only at X coordinate */
    def fillOnlyX: this.type = {
      fill = 1 xy 0
      layoutUp()
      this
    }

    /** Sets the box to take all available space only at Y coordinate */
    def fillOnlyY: this.type = {
      fill = 0 xy 1
      layoutUp()
      this
    }

    /** Sets the box to not fill any coordinate */
    def fillNone: this.type = {
      fill = 0 xy 0
      layoutUp()
      this
    }

    /** Sets the box to fill both coordinates */
    def fillBoth: this.type = {
      fill = 1 xy 1
      layoutUp()
      this
    }

    /** Sets the alignment of this box within parent bounds */
    def align(align: Vec2d): this.type = {
      ialign = align
      layoutUp()
      this
    }

    /** Aligns the box at the top */
    def alignTop: this.type = this.align(Vec2d.Top)
  }

  /** Represents box with children */
  trait ContainerBox extends LayoutBox {
    private[layout] var ipad: Vec2d = Vec2d.Zero
    private[layout] var ispace: Vec2d = Vec2d.Zero

    override def minimumSize: Vec2d = {
      val spaceSum = ispace * ((children.size - 1) max 0)
      val childSum = children.map(c => c.minimumSize).foldLeft(Vec2d.Zero) { case (sum, size) => sum + size }
      childSum + ipad * 2 + spaceSum
    }

    /** Changes the spacing between children to given value */
    def space(space: Vec2d): this.type = {
      this.ispace = space
      layoutUp()
      this
    }

    /** Changes the padding of the box to given value */
    def pad(pad: Vec2d): this.type = {
      this.ipad = pad
      layoutUp()
      this
    }

    /** Changes the padding of the box to given value */
    def pad(pad: Double): this.type = {
      this.pad(pad xy pad)
    }

    /** Adds the children to the box */
    def withChildren(children: LayoutBox*): this.type = this.synchronized {
      this.children = children.toList
      layoutUp()
      this
    }
  }

  /** Represents box with fixed size */
  case class FixedBox() extends ContainerBox {
    private var box: Rect2d = Rect2d.Zero

    private def layoutChildren(): Unit = {
      children.foreach { child =>
        child.layoutDown(child.alignWithin(
          box
            .offsetBy(ipad)
            .resizeTo(box.size - ipad * 2)
        ))
      }
    }

    override def layoutUp(): Unit = {
      layoutChildren()
    }

    override def layoutDown(box: Rect2d): Unit = {
      this.box = box
      layoutChildren()
    }

    /** Changes the size of the fixed box to a given value */
    def resizeTo(size: Vec2d): this.type = {
      this.box = box.resizeTo(size)
      super.layoutUp()
      this
    }

    override def minimumSize: Vec2d = box.size
  }


  /** Layouts children in horizontal line */
  case class XBox() extends ContainerBox {
    private var ifixedHeight: Option[Double] = None
    this.fillX

    override def layoutDown(box: Rect2d): Unit = {
      val fillCount = children.count(c => c.fill.x == 1)
      val fillX = if (fillCount > 0) ((box.size.x - minimumSize.x) max 0) / fillCount else 0
      children.foldLeft(ipad.x) { case (x, child) =>
        val s = child.minimumSize
        val w = if (child.fill.x == 1) s.x + fillX else s.x
        child.layoutDown(child.alignWithin(
          box
            .resizeTo(w xy (box.size.y - ipad.y * 2))
            .offsetBy(x xy ipad.y)
        ))
        x + w + ispace.x
      }
    }

    /** Changes the X spacing between children to given value */
    def space(space: Double): this.type = {
      this.space(space xy 0)
    }

    /** Defines the fixed height of the box */
    def fixedHeight(height: Double): this.type = {
      ifixedHeight = Some(height)
      layoutUp()
      this
    }

    override def minimumSize: Vec2d = {
      val size = super.minimumSize
      ifixedHeight.map(h => size.copy(y = h)).getOrElse(size)
    }
  }

  /** Layouts children in horizontal line */
  case class YBox() extends ContainerBox {
    this.fillY

    override def layoutDown(box: Rect2d): Unit = {
      val fillCount = children.count(c => c.fill.y == 1)
      val fillY = if (fillCount > 0) ((box.size.y - minimumSize.y) max 0) / fillCount else 0
      children.foldLeft(ipad.y) { case (y, child) =>
        val s = child.minimumSize
        val h = if (child.fill.x == 1) s.y + fillY else s.y
        child.layoutDown(child.alignWithin(
          box
            .resizeTo((box.size.x - ipad.x * 2) xy h)
            .offsetBy(ipad.x xy y)
        ))
        y + h + ispace.y
      }
    }

    /** Changes the Y spacing between children to given value */
    def space(space: Double): this.type = {
      this.space(0 xy space)
    }
  }

  /** Stacks the children on top of each other */
  case class StackBox() extends ContainerBox {
    override def layoutDown(box: Rect2d): Unit = {
      val targetSize = children.foldLeft(Vec2d.Zero) { case (maxSize, child) => maxSize maxVec child.minimumSize }
      val targetBox = alignWithin(box, Some(targetSize))
      children.foreach(child => child.layoutDown(child.alignWithin(targetBox)))
    }
  }

  /** Fills the space between other components */
  case class EmptyBox() extends LayoutBox {
    override def layoutDown(box: Rect2d): Unit = children.foreach(c => c.layoutDown(c.alignWithin(box)))

    override def minimumSize: Vec2d = Vec2d.Zero
  }

  /** Fills the horizontal space between components */
  val XFiller: EmptyBox = EmptyBox().fillX
  /** Fills the vertical space between components */
  val YFiller: EmptyBox = EmptyBox().fillY
  /** Fills all space between components */
  val Filler: EmptyBox = EmptyBox().fillBoth

}