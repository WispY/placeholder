package cross.pac

import cross.common._
import cross.component.flat.Button.ButtonStyle
import cross.component.util._
import cross.config._
import cross.format._

object config {

  /** Configures pac project */
  case class PacConfig(globalStage: PacGlobalStageConfig)

  /** Configures global stage of pac project */
  case class PacGlobalStageConfig(stagePad: Vec2d,
                                  stageSpace: Vec2d,
                                  stageColor: Color,
                                  stageShadow: Color,
                                  stageShadowSize: Double,
                                  welcomeButtonStyle: ButtonStyle,
                                  welcomeLabelStyle: FontStyle,
                                  userStyle: FontStyle,
                                  signinButtonStyle: ButtonStyle,
                                  signinLabelStyle: FontStyle,
                                  manageButtonStyle: ButtonStyle,
                                  manageLabelStyle: FontStyle)

  implicit val colorFormat: CF[Color] = stringFormat.map(v => Colors.hex(v), v => v.toHex)
  implicit val fontFormat: CF[Font] = stringFormat.map(v => Font(v), v => v.family)
  implicit val vec2dFormat: CF[Vec2d] = format2(Vec2d.apply)
  implicit val vec2iFormat: CF[Vec2i] = format2(Vec2i.apply)
  implicit val fontStyleFormat: CF[FontStyle] = format4(FontStyle)
  implicit val buttonStyleFormat: CF[ButtonStyle] = format5(ButtonStyle)

  implicit val pacGlobalStageConfigFormat: CF[PacGlobalStageConfig] = format12(PacGlobalStageConfig)
  implicit val pacConfigFormat: CF[PacConfig] = format1(PacConfig)

  val FlatFontStyle = FontStyle(
    font = RobotoSlab,
    size = 20,
    align = Vec2d.Center,
    fill = Colors.PureWhite
  )
  val PopFontStyle: FontStyle = FlatFontStyle.copy(
    fill = Colors.GreenDarkest
  )
  val PopButtonStyle = ButtonStyle(
    colorNormal = Colors.Green,
    colorHover = Colors.GreenLight,
    colorPressed = Colors.GreenLight,
    colorDisabled = Colors.Gray,
    depth = 3
  )
  val FlatButtonStyle = ButtonStyle(
    colorNormal = Colors.BlueDarkest,
    colorHover = Colors.BlueDark,
    colorPressed = Colors.Blue,
    colorDisabled = Colors.BlueDarkest,
    depth = 0
  )
  val DefaultConfig = PacConfig(
    globalStage = PacGlobalStageConfig(
      stagePad = 15 xy 10,
      stageSpace = 10 xy 10,
      stageColor = Colors.BlueDarkest,
      stageShadow = Colors.Black.tint(Colors.PureBlack, 0.25),
      stageShadowSize = 3,
      welcomeButtonStyle = FlatButtonStyle,
      welcomeLabelStyle = FlatFontStyle,
      userStyle = FlatFontStyle,
      signinButtonStyle = PopButtonStyle,
      signinLabelStyle = PopFontStyle,
      manageButtonStyle = FlatButtonStyle,
      manageLabelStyle = FlatFontStyle
    )
  )

  val Config: PacConfig = configureNamespace("pac", Some(DefaultConfig))
}