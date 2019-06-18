package cross

import cross.common._

object layout {

  /** Fills all space between components */
  def filler: StackBox = new StackBox().fillBoth

  /** Creates a horizontal box */
  def xbox: XBox = new XBox()

  /** Creates a vertical box */
  def ybox: YBox = new YBox()

  /** Creates an empty box */
  def box: StackBox = new StackBox()

  /** Represents a 2D object with layout properties */
  trait LayoutBox {
    private[layout] var layoutEnabled: Boolean = false
    private[layout] var layoutVisible: Boolean = true
    private[layout] var layoutFill: Vec2i = Vec2i.Zero
    private[layout] var layoutChildren: List[LayoutBox] = Nil
    private[layout] var layoutParent: Option[LayoutBox] = None
    private[layout] var layoutAlign: Vec2d = Vec2d.Center
    private[layout] var layoutSize: Vec2d = Vec2d.Zero
    private[layout] var layoutPad: Vec2d = Vec2d.Zero
    private[layout] var layoutSpace: Vec2d = Vec2d.Zero
    private[layout] var layoutBounds: Option[Rec2d] = None
    private[layout] var layoutBoxMapper: Rec2d => Rec2d = { box => box }

    /** Adds the child to this box */
    def add(child: LayoutBox): this.type = this.synchronized {
      layoutChildren = layoutChildren :+ child
      child.layoutParent = Some(this)
      if (layoutEnabled) child.layout(propagate = false)
      layoutUp()
      this
    }

    /** Removes the child from this box */
    def remove(child: LayoutBox): this.type = this.synchronized {
      layoutChildren = layoutChildren.filterNot(c => c eq child)
      child.layoutParent = None
      layoutUp()
      this
    }

    /** Removes this box from the parent */
    def detach: this.type = this.synchronized {
      layoutParent.foreach(p => p.remove(this))
      layoutUp()
      this
    }

    /** Propagates the layout request up the parent chain */
    def layoutUp(): Unit = layoutParent match {
      case _ if !layoutEnabled => // ignore
      case Some(p) => p.layoutUp()
      case None => layoutDown(Rec2d(Vec2d.Zero, minimumSize))
    }

    /** Propagates the layout request internally */
    private[layout] def layoutDownInternal(box: Rec2d): Unit = {
      layoutBounds = Some(box)
      layoutDown(layoutBoxMapper.apply(box))
    }

    /** Re-layouts the component and it's children */
    def reLayoutDown(): Unit = layoutBounds match {
      case Some(box) => layoutDownInternal(box)
      case None => layoutUp()
    }

    /** Updates the layout of the box within given bounds */
    def layoutDown(box: Rec2d): Unit

    /** Aligns the box within the given bounds */
    def alignWithin(box: Rec2d, forcedSize: Option[Vec2d] = None): Rec2d = {
      val minSize = minimumSize
      val targetSize = forcedSize.getOrElse((if (layoutFill.x == 1) box.size.x else minSize.x) xy (if (layoutFill.y == 1) box.size.y else minSize.y))
      val targetOffset = layoutAlign * (box.size - targetSize)
      box.offsetBy(targetOffset).resizeTo(targetSize)
    }

    /** Returns the minimum dimensions of this box */
    def minimumSize: Vec2d

    /** Merges the fixed size with the given size */
    private[layout] def mergeSize(other: Vec2d): Vec2d = {
      val x = if (layoutSize.x > 0) layoutSize.x else other.x
      val y = if (layoutSize.y > 0) layoutSize.y else other.y
      x xy y
    }

    /** Sets the fixed size for the box */
    def size(size: Vec2d): this.type = {
      layoutSize = size
      layoutUp()
      this
    }

    /** Changes the width of the box to a fixed value */
    def width(width: Double): this.type = this.size(width xy layoutSize.y)

    /** Changes the height of the box to a fixed value */
    def height(height: Double): this.type = this.size(layoutSize.x xy height)

    /** Sets the box to take all available space at X coordinate */
    def fillX: this.type = {
      layoutFill = 1 xy layoutFill.y
      layoutUp()
      this
    }

    /** Sets the box to take all available space at Y coordinate */
    def fillY: this.type = {
      layoutFill = layoutFill.x xy 1
      layoutUp()
      this
    }

    /** Sets the box to take all available space only at X coordinate */
    def fillOnlyX: this.type = {
      layoutFill = 1 xy 0
      layoutUp()
      this
    }

    /** Sets the box to take all available space only at Y coordinate */
    def fillOnlyY: this.type = {
      layoutFill = 0 xy 1
      layoutUp()
      this
    }

    /** Sets the box to not fill any coordinate */
    def fillNone: this.type = {
      layoutFill = 0 xy 0
      layoutUp()
      this
    }

    /** Sets the box to fill both coordinates */
    def fillBoth: this.type = {
      layoutFill = 1 xy 1
      layoutUp()
      this
    }

    /** Sets the alignment of this box within parent bounds */
    def align(align: Vec2d): this.type = {
      layoutAlign = align
      layoutUp()
      this
    }

    /** Aligns the box at the top left */
    def alignTopLeft: this.type = this.align(Vec2d.TopLeft)

    /** Aligns the box at the top */
    def alignTop: this.type = this.align(Vec2d.Top)

    /** Aligns the box at the top right */
    def alignTopRight: this.type = this.align(Vec2d.TopRight)

    /** Aligns the box at the left */
    def alignLeft: this.type = this.align(Vec2d.Left)

    /** Aligns the box at the center */
    def alignCenter: this.type = this.align(Vec2d.Center)

    /** Aligns the box at the right */
    def alignRight: this.type = this.align(Vec2d.Right)

    /** Aligns the box at the bottom left */
    def alignBottomLeft: this.type = this.align(Vec2d.BottomLeft)

    /** Aligns the box at the bottom */
    def alignBottom: this.type = this.align(Vec2d.Bottom)

    /** Aligns the box at the bottom right */
    def alignBottomRight: this.type = this.align(Vec2d.BottomRight)

    /** Changes the spacing between children to given value */
    def space(space: Vec2d): this.type = {
      layoutSpace = space
      layoutUp()
      this
    }

    /** Changes the padding of the box to given value */
    def pad(pad: Vec2d): this.type = {
      layoutPad = pad
      layoutUp()
      this
    }

    /** Changes the padding of the box to given value */
    def pad(pad: Double): this.type = {
      this.pad(pad xy pad)
    }

    /** Returns the current padding of the box */
    def getPad: Vec2d = layoutPad

    /** Adds the children to the box */
    def children(children: LayoutBox*): this.type = this.synchronized {
      layoutChildren = children.toList
      children.foreach(c => c.layoutParent = Some(this))
      if (layoutEnabled) layoutChildren.foreach(c => c.layout(propagate = false))
      layoutUp()
      this
    }

    /** Returns only visible children */
    private[layout] def visibleChildren: List[LayoutBox] = layoutChildren.filter(c => c.layoutVisible)

    /** Sets the bounds mapping function for effects on box applied to the layout */
    def mapBounds(code: Rec2d => Rec2d): this.type = {
      layoutBoxMapper = code
      reLayoutDown()
      this
    }

    /** Enables the layout of this box */
    def layout(propagate: Boolean = true): this.type = {
      layoutEnabled = true
      layoutChildren.foreach(c => c.layout(propagate = false))
      if (layoutEnabled) layoutUp()
      this
    }

    /** Sets the visibility of the box to a given value */
    def visible(visible: Boolean): this.type = {
      layoutVisible = visible
      propagateVisibility(areParentsVisible)
      if (layoutEnabled) layoutUp()
      this
    }

    /** Returns true when all parents are visible */
    private[layout] def areParentsVisible: Boolean = layoutParent match {
      case None => true
      case Some(parent) => parent.layoutVisible && parent.areParentsVisible
    }

    /** Propagates the visibility update down the tree */
    private[layout] def propagateVisibility(parentVisible: Boolean): Unit = {
      handleVisibility(layoutVisible, parentVisible)
      layoutChildren.foreach(c => c.propagateVisibility(parentVisible && layoutVisible))
    }

    /** Allows to customize how visibility is handled */
    def handleVisibility(selfVisible: Boolean, parentVisible: Boolean): Unit = {}
  }


  /** Layouts children in horizontal line */
  class XBox() extends LayoutBox {
    this.fillX

    override def layoutDown(box: Rec2d): Unit = {
      val fillCount = visibleChildren.count(c => c.layoutFill.x == 1)
      val fillX = if (fillCount > 0) ((box.size.x - minimumSize.x) max 0) / fillCount else 0
      visibleChildren.foldLeft(layoutPad.x) { case (x, child) =>
        val s = child.minimumSize
        val w = if (child.layoutFill.x == 1) s.x + fillX else s.x
        child.layoutDownInternal(child.alignWithin(
          box
            .resizeTo(w xy (box.size.y - layoutPad.y * 2))
            .offsetBy(x xy layoutPad.y)
        ))
        x + w + layoutSpace.x
      }
    }

    /** Changes the X spacing between children to given value */
    def space(space: Double): this.type = {
      this.space(space xy 0)
    }

    override def minimumSize: Vec2d = {
      val spaceSum = (layoutSpace.x * ((visibleChildren.size - 1) max 0)) xy 0
      val childSum = visibleChildren.map(c => c.minimumSize).foldLeft(Vec2d.Zero) { case (sum, size) => (sum.x + size.x) xy (sum.y max size.y) }
      mergeSize(childSum + layoutPad * 2 + spaceSum)
    }
  }

  /** Layouts children in horizontal line */
  class YBox() extends LayoutBox {
    this.fillY

    override def layoutDown(box: Rec2d): Unit = {
      val fillCount = visibleChildren.count(c => c.layoutFill.y == 1)
      val fillY = if (fillCount > 0) ((box.size.y - minimumSize.y) max 0) / fillCount else 0
      visibleChildren.foldLeft(layoutPad.y) { case (y, child) =>
        val s = child.minimumSize
        val h = if (child.layoutFill.y == 1) s.y + fillY else s.y
        child.layoutDownInternal(child.alignWithin(
          box
            .resizeTo((box.size.x - layoutPad.x * 2) xy h)
            .offsetBy(layoutPad.x xy y)
        ))
        y + h + layoutSpace.y
      }
    }

    /** Changes the Y spacing between children to given value */
    def space(space: Double): this.type = {
      this.space(0 xy space)
    }

    override def minimumSize: Vec2d = {
      val spaceSum = 0 xy (layoutSpace.y * ((visibleChildren.size - 1) max 0))
      val childSum = visibleChildren.map(c => c.minimumSize).foldLeft(Vec2d.Zero) { case (sum, size) => (sum.x max size.x) xy (sum.y + size.y) }
      mergeSize(childSum + layoutPad * 2 + spaceSum)
    }
  }

  /** Stacks the children on top of each other */
  class StackBox() extends LayoutBox {
    override def layoutDown(box: Rec2d): Unit = {
      val padded = box.resizeTo(box.size - layoutPad * 2).offsetBy(layoutPad)
      visibleChildren.foreach(child => child.layoutDownInternal(child.alignWithin(padded)))
    }

    override def minimumSize: Vec2d = {
      val targetSize = visibleChildren.foldLeft(Vec2d.Zero) { case (maxSize, child) => maxSize maxVec child.minimumSize }
      mergeSize(targetSize + layoutPad * 2)
    }
  }

}