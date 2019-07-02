package cross

import cross.box._

class BoxSpec extends Spec {
  "box" can {
    "assign single hierarchy" in {
      val a = container()
      val b = container()
      a.withChildren(b)
      a.layout.relativeChildren() shouldBe (b :: Nil)
      a.layout.absoluteChildren() shouldBe (b :: Nil)
      b.layout.relativeParents() shouldBe (a :: Nil)
      b.layout.absoluteParents() shouldBe (a :: Nil)

      a.withChildren()
      a.layout.relativeChildren() shouldBe Nil
      a.layout.absoluteChildren() shouldBe Nil
      b.layout.relativeParents() shouldBe Nil
      b.layout.absoluteParents() shouldBe Nil

      b.withChildren(a)
      b.layout.relativeChildren() shouldBe (a :: Nil)
      b.layout.absoluteChildren() shouldBe (a :: Nil)
      a.layout.relativeParents() shouldBe (b :: Nil)
      a.layout.absoluteParents() shouldBe (b :: Nil)
    }

    "assign 2-deep hierarchy" in {
      val a = container()
      val b = container()
      val c = container()

      a.withChildren(
        b.withChildren(c)
      )
      a.layout.relativeChildren() shouldBe (b :: Nil)
      a.layout.absoluteChildren() shouldBe (b :: c :: Nil)
      b.layout.relativeChildren() shouldBe (c :: Nil)
      b.layout.absoluteChildren() shouldBe (c :: Nil)
      b.layout.relativeParents() shouldBe (a :: Nil)
      b.layout.absoluteParents() shouldBe (a :: Nil)
      c.layout.relativeParents() shouldBe (b :: Nil)
      c.layout.absoluteParents() shouldBe (b :: a :: Nil)

      a.withChildren()
      b.withChildren()
      a.layout.relativeChildren() shouldBe Nil
      a.layout.absoluteChildren() shouldBe Nil
      b.layout.relativeChildren() shouldBe Nil
      b.layout.absoluteChildren() shouldBe Nil
      b.layout.relativeParents() shouldBe Nil
      b.layout.absoluteParents() shouldBe Nil
      c.layout.relativeParents() shouldBe Nil
      c.layout.absoluteParents() shouldBe Nil

      c.withChildren(
        b.withChildren(a)
      )
      c.layout.relativeChildren() shouldBe (b :: Nil)
      c.layout.absoluteChildren() shouldBe (b :: a :: Nil)
      b.layout.relativeChildren() shouldBe (a :: Nil)
      b.layout.absoluteChildren() shouldBe (a :: Nil)
      b.layout.relativeParents() shouldBe (c :: Nil)
      b.layout.absoluteParents() shouldBe (c :: Nil)
      a.layout.relativeParents() shouldBe (b :: Nil)
      a.layout.absoluteParents() shouldBe (b :: c :: Nil)
    }

    "assign 2-wide hierarchy" in {
      val a = container()
      val b = container()
      val c = container()

      a.withChildren(b, c)

      a.layout.relativeChildren() shouldBe (b :: c :: Nil)
      a.layout.absoluteChildren() shouldBe (b :: c :: Nil)
      b.layout.relativeParents() shouldBe (a :: Nil)
      b.layout.absoluteParents() shouldBe (a :: Nil)
      c.layout.relativeParents() shouldBe (a :: Nil)
      c.layout.absoluteParents() shouldBe (a :: Nil)

      a.withChildren()
      a.layout.relativeChildren() shouldBe Nil
      a.layout.absoluteChildren() shouldBe Nil
      b.layout.relativeParents() shouldBe Nil
      b.layout.absoluteParents() shouldBe Nil
      c.layout.relativeParents() shouldBe Nil
      c.layout.absoluteParents() shouldBe Nil
    }
  }
}