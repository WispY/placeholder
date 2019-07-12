package cross

import cross.box._
import cross.common._

class BoxSpec extends Spec {
  "box" can {
    "assign single hierarchy" in {
      implicit val styler: Styler = Styler.Empty
      val a = container()
      val b = container()
      a.withChildren(b)
      a.layout.relChildren() shouldBe (b :: Nil)
      a.layout.absChildren() shouldBe (b :: Nil)
      b.layout.relParents() shouldBe (a :: Nil)
      b.layout.absParents() shouldBe (a :: Nil)

      a.withChildren()
      a.layout.relChildren() shouldBe Nil
      a.layout.absChildren() shouldBe Nil
      b.layout.relParents() shouldBe Nil
      b.layout.absParents() shouldBe Nil

      b.withChildren(a)
      b.layout.relChildren() shouldBe (a :: Nil)
      b.layout.absChildren() shouldBe (a :: Nil)
      a.layout.relParents() shouldBe (b :: Nil)
      a.layout.absParents() shouldBe (b :: Nil)
    }

    "assign 2-deep hierarchy" in {
      implicit val styler: Styler = Styler.Empty
      val a = container()
      val b = container()
      val c = container()

      a.withChildren(
        b.withChildren(c)
      )
      a.layout.relChildren() shouldBe (b :: Nil)
      a.layout.absChildren() shouldBe (b :: c :: Nil)
      b.layout.relChildren() shouldBe (c :: Nil)
      b.layout.absChildren() shouldBe (c :: Nil)
      b.layout.relParents() shouldBe (a :: Nil)
      b.layout.absParents() shouldBe (a :: Nil)
      c.layout.relParents() shouldBe (b :: Nil)
      c.layout.absParents() shouldBe (b :: a :: Nil)

      a.withChildren()
      b.withChildren()
      a.layout.relChildren() shouldBe Nil
      a.layout.absChildren() shouldBe Nil
      b.layout.relChildren() shouldBe Nil
      b.layout.absChildren() shouldBe Nil
      b.layout.relParents() shouldBe Nil
      b.layout.absParents() shouldBe Nil
      c.layout.relParents() shouldBe Nil
      c.layout.absParents() shouldBe Nil

      c.withChildren(
        b.withChildren(a)
      )
      c.layout.relChildren() shouldBe (b :: Nil)
      c.layout.absChildren() shouldBe (b :: a :: Nil)
      b.layout.relChildren() shouldBe (a :: Nil)
      b.layout.absChildren() shouldBe (a :: Nil)
      b.layout.relParents() shouldBe (c :: Nil)
      b.layout.absParents() shouldBe (c :: Nil)
      a.layout.relParents() shouldBe (b :: Nil)
      a.layout.absParents() shouldBe (b :: c :: Nil)
    }

    "assign 2-wide hierarchy" in {
      implicit val styler: Styler = Styler.Empty
      val a = container()
      val b = container()
      val c = container()

      a.withChildren(b, c)

      a.layout.relChildren() shouldBe (b :: c :: Nil)
      a.layout.absChildren() shouldBe (b :: c :: Nil)
      b.layout.relParents() shouldBe (a :: Nil)
      b.layout.absParents() shouldBe (a :: Nil)
      c.layout.relParents() shouldBe (a :: Nil)
      c.layout.absParents() shouldBe (a :: Nil)

      a.withChildren()
      a.layout.relChildren() shouldBe Nil
      a.layout.absChildren() shouldBe Nil
      b.layout.relParents() shouldBe Nil
      b.layout.absParents() shouldBe Nil
      c.layout.relParents() shouldBe Nil
      c.layout.absParents() shouldBe Nil
    }

    trait ContainerTree {
      val idA = BoxId("A")
      val idB = BoxId("B")
      val idC = BoxId("C")

      implicit val s: Styler = styler
      val boxC: ContainerBox = container(id = idC)
      val boxB: ContainerBox = container(id = idB).withChildren(boxC)
      val boxA: ContainerBox = container(id = idA).withChildren(boxB)

      def styler: Styler = Styler.Empty
    }

    "layout empty containers" in new ContainerTree {
      override def styler: Styler = Styler.Empty

      boxA.layout.absBounds() shouldBe Rec2d.Zero
      boxB.layout.absBounds() shouldBe Rec2d.Zero
      boxC.layout.absBounds() shouldBe Rec2d.Zero
    }

    "pad containers" in new ContainerTree {
      override def styler: Styler = Styler.Empty

      boxC.pad(0.5 xy 1)
      boxB.pad(1 xy 2)
      boxA.pad(1.5 xy 3)

      boxA.layout.absBounds() shouldBe Rec2d(0 xy 0, 6 xy 12)
      boxB.layout.absBounds() shouldBe Rec2d(1.5 xy 3, 3 xy 6)
      boxC.layout.absBounds() shouldBe Rec2d(2.5 xy 5, 1 xy 2)
    }

    "pad containers with style" in new ContainerTree {
      override def styler: Styler = StyleSheet(
        isA[ContainerBox] /> { case container: ContainerBox => container.pad(1 xy 1) },
        hasId(idB) /> { case container: ContainerBox => container.pad(2 xy 2) },
        hasId(idA) /> { case container: ContainerBox => container.pad(3 xy 3) }
      )

      boxA.layout.absBounds() shouldBe Rec2d(0 xy 0, 12 xy 12)
      boxB.layout.absBounds() shouldBe Rec2d(3 xy 3, 6 xy 6)
      boxC.layout.absBounds() shouldBe Rec2d(5 xy 5, 2 xy 2)
    }

    "handle id style" in new ContainerTree {
      override def styler: Styler = StyleSheet(
        idB /> { case container: ContainerBox => container.pad(2 xy 2) }
      )

      boxA.layout.absBounds() shouldBe Rec2d(0 xy 0, 4 xy 4)
      boxB.layout.absBounds() shouldBe Rec2d(0 xy 0, 4 xy 4)
      boxC.layout.absBounds() shouldBe Rec2d(2 xy 2, 0 xy 0)
    }

    "handle parent style" in new ContainerTree {
      override def styler: Styler = StyleSheet(
        hasAbsParent(idA) /> { case container: ContainerBox => container.pad(1 xy 1) }
      )

      boxA.layout.absBounds() shouldBe Rec2d(0 xy 0, 4 xy 4)
      boxB.layout.absBounds() shouldBe Rec2d(0 xy 0, 4 xy 4)
      boxC.layout.absBounds() shouldBe Rec2d(1 xy 1, 2 xy 2)
    }

    "handle and style" in new ContainerTree {
      override def styler: Styler = StyleSheet(
        (hasAbsParent(idA) && hasAbsChild(idC)) /> { case container: ContainerBox => container.pad(1 xy 1) }
      )

      boxA.layout.absBounds() shouldBe Rec2d(0 xy 0, 2 xy 2)
      boxB.layout.absBounds() shouldBe Rec2d(0 xy 0, 2 xy 2)
      boxC.layout.absBounds() shouldBe Rec2d(1 xy 1, 0 xy 0)
    }
  }
}