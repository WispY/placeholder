package cross.component

import cross.common._
import cross.pixi.TextStyle

object util {

  /** Contains color values */
  case class Color(r: Double, g: Double, b: Double, a: Double) {
    /** Converts the color to a single RGB integer */
    def toInt: Int = 65536 * r.toInt + 256 * g.toInt + b.toInt

    /** Converts the color to a single RGB double */
    def toDouble: Double = toInt

    /** Converts the color to a single hex string */
    def toHex: String = f"#${r.toInt}%02x${g.toInt}%02x${b.toInt}%02x${a.toInt}%02x"

    /** Tins the color with given color by the given factor */
    def tint(other: Color, factor: Double): Color = Color(
      (r, other.r) %% factor,
      (g, other.g) %% factor,
      (b, other.b) %% factor,
      (a, other.a) %% factor
    )
  }

  object Colors {
    /** Parses color from hex string */
    def hex(string: String): Color = {
      val clear = string.replace("#", "")
      clear match {
        case hex if hex.length == 6 =>
          Color(
            r = Integer.valueOf(hex.substring(0, 2), 16).toDouble,
            g = Integer.valueOf(hex.substring(2, 4), 16).toDouble,
            b = Integer.valueOf(hex.substring(4, 6), 16).toDouble,
            a = 255
          )
        case hex if hex.length == 8 =>
          Color(
            r = Integer.valueOf(hex.substring(0, 2), 16).toDouble,
            g = Integer.valueOf(hex.substring(2, 4), 16).toDouble,
            b = Integer.valueOf(hex.substring(4, 6), 16).toDouble,
            a = Integer.valueOf(hex.substring(6, 8), 16).toDouble
          )
      }
    }

    /** DB32 #1 */
    val PureBlack: Color = hex("#000000")
    /** DB32 #2 */
    val Black: Color = hex("#222034")
    /** DB32 #3 */
    val PurpleDark: Color = hex("#45283c")
    /** DB32 #4 */
    val BrownDark: Color = hex("#663931")
    /** DB32 #5 */
    val Brown: Color = hex("#8f563b")
    /** DB32 #6 */
    val Orange: Color = hex("#df7126")
    /** DB32 #7 */
    val BrownLight: Color = hex("#d9a066")
    /** DB32 #8 */
    val BrownLightest: Color = hex("#eec39a")
    /** DB32 #9 */
    val Yellow: Color = hex("#fbf236")
    /** DB32 #10 */
    val GreenLight: Color = hex("#99e550")
    /** DB32 #11 */
    val Green: Color = hex("#6abe30")
    /** DB32 #12 */
    val Aqua: Color = hex("#37946e")
    /** DB32 #13 */
    val GreenDark: Color = hex("#4b692f")
    /** DB32 #14 */
    val OliveDark: Color = hex("#524b24")
    /** DB32 #15 */
    val GreenDarkest: Color = hex("#323c39")
    /** DB32 #16 */
    val BlueDarkest: Color = hex("#3f3f74")
    /** DB32 #17 */
    val AquaDark: Color = hex("#306082")
    /** DB32 #18 */
    val BlueDark: Color = hex("#5b6ee1")
    /** DB32 #19 */
    val Blue: Color = hex("#639bff")
    /** DB32 #20 */
    val BlueLight: Color = hex("#5fcde4")
    /** DB32 #21 */
    val BlueLightest: Color = hex("#cbdbfc")
    /** DB32 #22 */
    val PureWhite: Color = hex("#ffffff")
    /** DB32 #23 */
    val GrayLight: Color = hex("#9badb7")
    /** DB32 #24 */
    val Gray: Color = hex("#847e87")
    /** DB32 #25 */
    val GrayDark: Color = hex("#696a6a")
    /** DB32 #26 */
    val GrayDarkest: Color = hex("#595652")
    /** DB32 #27 */
    val Purple: Color = hex("#76428a")
    /** DB32 #28 */
    val RedDark: Color = hex("#ac3232")
    /** DB32 #29 */
    val Red: Color = hex("#d95763")
    /** DB32 #30 */
    val PurpleLight: Color = hex("#d77bba")
    /** DB32 #31 */
    val OliveLight: Color = hex("#8f974a")
    /** DB32 #32 */
    val Olive: Color = hex("#8a6f30")
  }

  implicit class ColorOps(val color: Color) extends AnyVal {
    /** Returns the darker version of this color */
    def darker: Color = color match {
      case Colors.Purple => Colors.Black
      case Colors.Brown => Colors.BrownDark
      case Colors.BrownLight => Colors.Brown
      case Colors.BrownLightest => Colors.BrownLight
      case Colors.GreenLight => Colors.Green
      case Colors.Green => Colors.GreenDark
      case Colors.Aqua => Colors.AquaDark
      case Colors.GreenDark => Colors.GreenDarkest
      case Colors.BlueDark => Colors.BlueDarkest
      case Colors.Blue => Colors.BlueDark
      case Colors.BlueLight => Colors.Blue
      case Colors.BlueLightest => Colors.BlueLight
      case Colors.PureWhite => Colors.GrayLight
      case Colors.GrayLight => Colors.Gray
      case Colors.Gray => Colors.GrayDark
      case Colors.GrayDark => Colors.GrayDarkest
      case Colors.Purple => Colors.PurpleDark
      case Colors.Red => Colors.RedDark
      case Colors.PurpleLight => Colors.Purple
      case Colors.OliveLight => Colors.Olive
      case Colors.Olive => Colors.OliveDark
      case other => other.tint(Colors.Black, 0.3)
    }

    /** Returns the lighter version of this color */
    def lighter: Color = color match {
      case Colors.PureBlack => Colors.GrayDarkest
      case Colors.Black => Colors.GrayDarkest
      case Colors.PurpleDark => Colors.Purple
      case Colors.BrownDark => Colors.Brown
      case Colors.Brown => Colors.BrownLight
      case Colors.BrownLight => Colors.BrownLightest
      case Colors.Green => Colors.GreenLight
      case Colors.GreenDark => Colors.Green
      case Colors.OliveDark => Colors.Olive
      case Colors.GreenDarkest => Colors.GreenDark
      case Colors.BlueDarkest => Colors.BlueDark
      case Colors.AquaDark => Colors.Aqua
      case Colors.BlueDark => Colors.Blue
      case Colors.Blue => Colors.BlueLight
      case Colors.BlueLight => Colors.BlueLightest
      case Colors.Gray => Colors.GrayLight
      case Colors.GrayDark => Colors.Gray
      case Colors.GrayDarkest => Colors.GrayDark
      case Colors.Purple => Colors.PurpleLight
      case Colors.RedDark => Colors.Red
      case Colors.Olive => Colors.OliveLight
      case other => other.tint(Colors.BrownLightest, 0.3)
    }
  }

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