package cross

import cross.box._

class BoxSpec extends Spec {
  "box" can {
    "assign single hierarchy" in {
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
  }
}