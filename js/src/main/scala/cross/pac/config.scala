package cross.pac

import cross.common._
import cross.component.flat.Button.ButtonStyle
import cross.component.flat.Paginator.PaginatorStyle
import cross.component.flat.ScrollArea.ScrollAreaStyle
import cross.component.util._
import cross.config._
import cross.format._

object config {

  /** Configures pac project */
  case class PacConfig(bar: BarConfig,
                       manage: ManageConfig)

  case class BarConfig(stagePad: Vec2d,
                       stageSpace: Vec2d,
                       barBackground: Color,
                       barShadow: Color,
                       barShadowSize: Double,
                       welcomeButtonStyle: ButtonStyle,
                       welcomeLabelStyle: FontStyle,
                       userStyle: FontStyle,
                       signinButtonStyle: ButtonStyle,
                       signinLabelStyle: FontStyle,
                       manageButtonStyle: ButtonStyle,
                       manageLabelStyle: FontStyle)

  case class ManageConfig(space: Vec2d,
                          challengesWidth: Double,
                          scroll: ScrollAreaStyle,
                          messagesLoadingLabelStyle: FontStyle,
                          messagesSpace: Vec2d,
                          messagesButtonStyle: ButtonStyle,
                          messagesPad: Vec2d,
                          messagesLabelStyle: FontStyle,
                          messagesMaxLength: Int,
                          messagesPaginatorStyle: PaginatorStyle)

  implicit val colorFormat: CF[Color] = stringFormat.map(v => Colors.hex(v), v => v.toHex)
  implicit val fontFormat: CF[Font] = stringFormat.map(v => Font(v), v => v.family)
  implicit val vec2dFormat: CF[Vec2d] = format2(Vec2d.apply)
  implicit val vec2iFormat: CF[Vec2i] = format2(Vec2i.apply)
  implicit val fontStyleFormat: CF[FontStyle] = format4(FontStyle)
  implicit val buttonStyleFormat: CF[ButtonStyle] = format5(ButtonStyle)
  implicit val scrollStyleFormat: CF[ScrollAreaStyle] = format7(ScrollAreaStyle)
  implicit val paginatorStyleFormat: CF[PaginatorStyle] = format6(PaginatorStyle)

  implicit val barConfigFormat: CF[BarConfig] = format12(BarConfig)
  implicit val manageConfigFormat: CF[ManageConfig] = format10(ManageConfig)
  implicit val pacConfigFormat: CF[PacConfig] = format2(PacConfig)

  val FlatFontStyle = FontStyle(
    font = RobotoSlab,
    size = 20,
    align = Vec2d.Center,
    fill = Colors.PureWhite
  )
  val SmallFlatFontStyle: FontStyle = FlatFontStyle.copy(size = 12)

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
  val ScrollStyle = ScrollAreaStyle(
    color = Colors.BlueDarkest,
    speed = 0.25,
    distance = 200,
    barWidth = 15,
    barMinLength = 50,
    space = 0,
    pad = 2
  )

  val DefaultConfig = PacConfig(
    BarConfig(
      stagePad = 15 xy 10,
      stageSpace = 10 xy 10,
      barBackground = Colors.BlueDarkest,
      barShadow = Colors.Black.tint(Colors.PureBlack, 0.25),
      barShadowSize = 3,
      welcomeButtonStyle = FlatButtonStyle,
      welcomeLabelStyle = FlatFontStyle,
      userStyle = FlatFontStyle,
      signinButtonStyle = PopButtonStyle,
      signinLabelStyle = PopFontStyle,
      manageButtonStyle = FlatButtonStyle,
      manageLabelStyle = FlatFontStyle
    ),
    ManageConfig(
      space = 10 xy 10,
      challengesWidth = 300,
      messagesLoadingLabelStyle = FlatFontStyle,
      scroll = ScrollStyle,
      messagesSpace = 0 xy 0,
      messagesButtonStyle = ButtonStyle(
        colorNormal = Colors.Black,
        colorHover = Colors.BlueDarkest,
        colorPressed = Colors.BlueDark,
        colorDisabled = Colors.Black,
        depth = 1
      ),
      messagesPad = 10 xy 5,
      messagesLabelStyle = SmallFlatFontStyle,
      messagesMaxLength = 1000,
      messagesPaginatorStyle = PaginatorStyle(
        pageSize = 16,
        space = 10 xy 10,
        pad = 15 xy 10,
        button = PopButtonStyle,
        buttonLabel = PopFontStyle,
        pageLabel = FlatFontStyle
      )
    )
  )

  val Config: PacConfig = configureNamespace("pac", Some(DefaultConfig))
}