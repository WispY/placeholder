package cross.component

import cross.common._
import cross.pixi.TextStyle

object util {

  /** Refers to a loaded font */
  case class Font(family: String)

  val Roboto = Font("Roboto")
  val RobotoSlab = Font("Roboto Slab")

  /** Represents the style of the text
    *
    * @param font  the reference to loaded font
    * @param size  the size of the text in px
    * @param align the horizontal alignment of the text
    * @param fill  the color used as main fill for the text
    */
  case class FontStyle(font: Font,
                       size: Double,
                       align: Vec2d,
                       fill: Color) {
    /** Aligns the text at the right */
    def alignRight: FontStyle = copy(align = Vec2d.Right)

    /** Aligns the text at the left */
    def alignLeft: FontStyle = copy(align = Vec2d.Left)

    /** Converts to pixi text style */
    def toTextStyle: TextStyle = new TextStyle().mutate { style =>
      style.fontFamily = font.family
      style.fontSize = size
      style.align = align match {
        case Vec2d.Left => "left"
        case Vec2d.Center => "center"
        case Vec2d.Right => "right"
      }
      style.fill = fill.toDouble
    }
  }

  /** Safe to use default font */
  val DefaultFont = Font("Arial")

}