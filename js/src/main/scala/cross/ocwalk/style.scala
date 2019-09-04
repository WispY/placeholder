package cross.ocwalk

import cross.box.ImageStyle._
import cross.box.StyleSheet
import cross.common._

/** Ocwalk application style */
//noinspection TypeAnnotation
object style {
  val primary = Color(1, 2, 3, 1)

  val tileset = Tileset("/tileset/ocwalk")

  val logo = tileset.source("/image/ocwalk/logo.png")
  val logoPrimary32 = logo.ref(size = 32 xy 32, color = primary)

  implicit val style = StyleSheet(

  )
}