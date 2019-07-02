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


    }
  }
}