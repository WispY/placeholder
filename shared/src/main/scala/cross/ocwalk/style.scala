package cross.ocwalk

import cross.box.ImageStyle._
import cross.box.StyleSheet
import cross.common._

/** Ocwalk application style */
//noinspection TypeAnnotation
object style {
  val primary = Colors.Green

  val tileset = Tileset("/tileset/ocwalk")

  val logo = tileset.source("/image/ocwalk/test-1.png")
  val logoBlackFull = logo.ref(color = _ => Colors.PureBlack)
  val logoPrimary32 = logo.ref(size = 32 xy 32, color = _ => primary)
  val logoWhite64 = logo.ref(size = 64 xy 64, color = _ => Colors.PureWhite)
  val logoBlack64 = logo.ref(size = 64 xy 64, color = _ => Colors.PureBlack)

  val test = tileset.source("/image/ocwalk/test-2.png")
  val testTintFull = test.ref(color = _.tint(Colors.BlueLight, 0.5))
  val test32 = test.ref(size = 32 xy 32)
  val testRed64 = test.ref(size = 64 xy 64, color = _ => Colors.Red)

  implicit val style = StyleSheet(

  )
}