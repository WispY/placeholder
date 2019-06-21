package cross

import cross.common._
import cross.layout._

class LayoutSpec extends Spec {
  "layout" can {
    "position probe at center" in {
      val probe = Probe(50 xy 50)
      box.size(100 xy 100).children(
        probe
      ).layout()
      probe.box shouldBe Rec2d(position = 25 xy 25, size = 50 xy 50)
    }

    "position probe at left via filler" in {
      val probe = Probe(50 xy 50)
      box.size(100 xy 100).children(
        xbox.children(
          probe,
          filler
        )
      ).layout()
      probe.box shouldBe Rec2d(position = 0 xy 25, size = 50 xy 50)
    }

    "position probe at right via filler" in {
      val probe = Probe(50 xy 50)
      box.size(100 xy 100).children(
        xbox.children(
          filler,
          probe
        )
      ).layout()
      probe.box shouldBe Rec2d(position = 50 xy 25, size = 50 xy 50)
    }

    "position probe in center via two fillers" in {
      val probe = Probe(50 xy 50)
      box.size(100 xy 100).children(
        xbox.children(
          filler,
          probe,
          filler
        )
      ).layout()
      probe.box shouldBe Rec2d(position = 25 xy 25, size = 50 xy 50)
    }

    "calculate top bar layout" in {
      val probeA = Probe(100 xy 0).fillY
      val probeB = Probe(50 xy 0).fillY
      val probeC = Probe(100 xy 0).fillY
      box.size(1000 xy 1000).children(
        ybox.fillBoth.children(
          xbox.alignTop.height(100).pad(10).space(10).children(
            probeA,
            filler,
            probeB,
            probeC
          ),
          filler
        )
      ).layout()
      probeA.box shouldBe Rec2d(position = 10 xy 10, size = 100 xy 80)
      probeB.box shouldBe Rec2d(position = 830 xy 10, size = 50 xy 80)
      probeC.box shouldBe Rec2d(position = 890 xy 10, size = 100 xy 80)
    }
  }

  /** Records the propagates layout size */
  case class Probe(size: Vec2d = Vec2d.Zero) extends LayoutBox {
    var box: Rec2d = Rec2d.Zero

    override def layoutDown(absolutePosition: Vec2d, box: Rec2d): Unit = this.box = box

    override def minimumSize: Vec2d = size
  }

}