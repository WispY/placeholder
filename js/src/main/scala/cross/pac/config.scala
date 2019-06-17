package cross.pac

import cross.common._
import cross.component.flat.Button.ButtonStyle
import cross.component.util.{Color, Colors, Font, FontStyle, Roboto}
import cross.config._
import cross.format._

object config {

  /** Configures pac project */
  case class PacConfig(globalStage: PacGlobalStageConfig)

  /** Configures global stage of pac project */
  case class PacGlobalStageConfig(stageHeight: Double,
                                  stagePad: Vec2d,
                                  stageSpace: Vec2d,
                                  welcomeStyle: FontStyle,
                                  userStyle: FontStyle,
                                  loginStyle: ButtonStyle,
                                  loginSize: Vec2i) {
    def stageHeightHalf: Double = stageHeight * 0.5
  }

  implicit val colorFormat: CF[Color] = stringFormat.map(v => Colors.hex(v), v => v.toHex)
  implicit val fontFormat: CF[Font] = stringFormat.map(v => Font(v), v => v.family)
  implicit val vec2dFormat: CF[Vec2d] = format2(Vec2d.apply)
  implicit val vec2iFormat: CF[Vec2i] = format2(Vec2i.apply)
  implicit val fontStyleFormat: CF[FontStyle] = format4(FontStyle)
  implicit val buttonStyleFormat: CF[ButtonStyle] = format6(ButtonStyle)

  implicit val pacGlobalStageConfigFormat: CF[PacGlobalStageConfig] = format7(PacGlobalStageConfig)
  implicit val pacConfigFormat: CF[PacConfig] = format1(PacConfig)

  val DefaultFontStyle = FontStyle(
    font = Roboto,
    size = 20,
    align = Vec2d.Center,
    fill = Colors.PureWhite
  )
  val DefaultButtonStyle = ButtonStyle(
    colorNormal = Colors.BlueDark,
    colorHover = Colors.Blue,
    colorPressed = Colors.Blue,
    colorDisabled = Colors.Gray,
    depth = 3,
    font = DefaultFontStyle
  )
  val DefaultConfig = PacConfig(
    globalStage = PacGlobalStageConfig(
      stageHeight = 60,
      stagePad = 10 xy 10,
      stageSpace = 10 xy 10,
      welcomeStyle = DefaultFontStyle.alignLeft,
      userStyle = DefaultFontStyle.alignRight,
      loginStyle = DefaultButtonStyle,
      loginSize = 100 xy 50
    )
  )

  val Config: PacConfig = configureNamespace("pac", Some(DefaultConfig))
}