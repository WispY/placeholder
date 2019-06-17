package cross

import cross.layout._
import cross.common._

class LayoutSpec extends Spec {
  "layout" can {
    "position probe at center" in {
      val probe = Probe(50 xy 50)
      FixedBox().resizeTo(100 xy 100).withChildren(
        probe
      )
      probe.box shouldBe Rect2d(position = 25 xy 25, size = 50 xy 50)
    }

    "position probe at left via filler" in {
      val probe = Probe(50 xy 50)
      FixedBox().resizeTo(100 xy 100).withChildren(
        XBox().withChildren(
          probe,
          Filler
        )
      )
      probe.box shouldBe Rect2d(position = 0 xy 25, size = 50 xy 50)
    }

    "position probe at right via filler" in {
      val probe = Probe(50 xy 50)
      FixedBox().resizeTo(100 xy 100).withChildren(
        XBox().withChildren(
          Filler,
          probe
        )
      )
      probe.box shouldBe Rect2d(position = 50 xy 25, size = 50 xy 50)
    }

    "position probe in center via two fillers" in {
      val probe = Probe(50 xy 50)
      FixedBox().resizeTo(100 xy 100).withChildren(
        XBox().withChildren(
          Filler,
          probe,
          Filler
        )
      )
      probe.box shouldBe Rect2d(position = 25 xy 25, size = 50 xy 50)
    }

    "calculate top bar layout" in {
      val probeA = Probe(100 xy 0).fillY
      val probeB = Probe(50 xy 0).fillY
      val probeC = Probe(100 xy 0).fillY
      FixedBox().resizeTo(1000 xy 1000).withChildren(
        YBox().fillBoth.withChildren(
          XBox().alignTop.fixedHeight(100).pad(10).space(10).withChildren(
            probeA,
            Filler,
            probeB,
            probeC
          ),
          Filler
        )
      )
      probeA.box shouldBe Rect2d(position = 10 xy 10, size = 100 xy 80)
    }
  }

  /** Records the propagates layout size */
  case class Probe(size: Vec2d = Vec2d.Zero) extends LayoutBox {
    var box: Rect2d = Rect2d.Zero

    override def layoutDown(box: Rect2d): Unit = this.box = box

    override def minimumSize: Vec2d = size
  }

}