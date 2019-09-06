package cross

import cross.common._
import cross.component.util._
import cross.general.config.{GeneralConfig, JsReader}
import cross.pixi.{ScaleModes, Settings}
import cross.processing.packer
import cross.sakura.stage.{GameStage, LoadingStage}
import cross.util.global.GlobalContext
import cross.util.http._
import cross.util.logging.Logging
import cross.util.mvc.Ui
import cross.util.{animation, fonts, spring}
import org.scalajs.dom._

import scala.concurrent.{ExecutionContext, Future}

/** Starts the UI application */
//noinspection TypeAnnotation
object app extends App with GlobalContext with Logging {
  override protected def logKey: String = "app"

  config.setGlobalReader(JsReader)
  implicit val generalConfig: GeneralConfig = general.config.Config

  window.location.pathname match {
    case sakura if sakura.startsWith("/sakura") =>
      startSakura()
    case pac if pac.startsWith("/pac") =>
      startPac(pac)
    case discord if discord.startsWith("/discord") =>
      queryParameter("code") match {
        case Some(code) => loginDiscord(code)
        case None =>
          log.warn("login error, redirecting to [/pac]")
          redirect("/pac")
      }
    case _ =>
      log.info("redirecting to [/pac]")
      redirect("/pac")
  }

  def startSakura(): Unit = {
    import cross.sakura.mvc._

    log.info("starting sakura project")
    Settings.SCALE_MODE = ScaleModes.NEAREST
    updateTitle("Sakura Challenge")
    implicit val model = Model()
    implicit val controller = new Controller(model)
    implicit val ec = ExecutionContext.global

    for {
      _ <- new Ui[Stages.Value]({ (stage, application) =>
        implicit val app = application
        stage match {
          case Stages.Loading => new LoadingStage()
          case Stages.Game => new GameStage()
        }
      }).load()
      _ <- spring.load()
      _ <- animation.load()
      _ <- controller.start()
    } yield ()
  }

  def startPac(path: String)(implicit generalConfig: GeneralConfig): Unit = {
    import cross.pac.mvc._

    log.info("starting pac project")
    updateTitle("Poku Art Challenge")
    implicit val pacConfig = pac.config.Config
    implicit val barConfig = pacConfig.bar
    implicit val manageConfig = pacConfig.manage
    implicit val model = Model()
    implicit val controller = new Controller(model)
    implicit val ec = ExecutionContext.global

    //    for {
    //      _ <- fonts.load(Roboto :: RobotoSlab :: Nil)
    //      _ <- new Ui[Unit]({ (stage, application) =>
    //        implicit val app = application
    //        stage match {
    //          case _ => new PacStage()
    //        }
    //      }).load()
    //      _ <- spring.load()
    //      _ <- animation.load()
    //      _ <- controller.start(path)
    //    } yield ()

    (for {
      _ <- fonts.load(Roboto :: RobotoSlab :: MaterialIcons :: Nil)
      _ <- testUi(controller)
      _ <- spring.load()
      _ <- animation.load()
      _ <- controller.start(path)
    } yield ()).whenFailed(up => log.error("failed to build ui", up))
  }

  def testUi(controller: cross.pac.mvc.Controller): Future[Unit] = Future {
    log.info("loading test ui")
    import cross.box._
    import cross.common._
    import jqbox._

    val refreshScreenSize = () => controller.setScreenSize(window.innerWidth.toInt xy window.innerHeight.toInt)
    window.addEventListener("resize", (_: Event) => refreshScreenSize(), useCapture = false)
    refreshScreenSize()
    scaleToScreen(controller)

    val fillId = BoxId("fill-box")

    implicit val styles: Styler = StyleSheet(
      isRegion && fillId |> {
        _.fillColor(Colors.RedDark.darker)
      }
    )

    val count = 100
    val delay = 500
    val parent = fbox(BoxId("free-box"))
    parent.layout.align.write(Vec2d.TopLeft)
    val regions = (0 until count)
      .map { i =>
        val box = region(BoxId(s"reg$i"))
        box.fillColor(Color(255, 255.0 * i / count, 0))
        box.layout.align.write(Vec2d.TopLeft)
        box
      }
      .toList

    val allRegions: Writeable[List[Vec2d]] = Data(Nil)
    val curRegions: Writeable[List[Vec2d]] = Data(Nil)
    val curAreas: Writeable[List[Rec2d]] = Data(Nil)

    controller.model.tick /~ { case t => t / delay } /> { case Some(t) =>
      log.info(s"all regions tick [$t]")
      allRegions.write(
        (0 until count)
          .map { i => (50 xy 50).toDouble.random(min = 5 xy 5) }
          .toList
          .sortBy(v => -v.x * v.y)
      )
    }

    controller.model.tick /~ { case t => (t % delay) / (delay / count) } /> { case Some(i) =>
      curRegions.write(allRegions().take(i.toInt))
      log.info(s"cur regions index [$i]: ${curRegions()}")
    }

    curRegions /> { case list =>
      val (total, areas) = packer.pack(list, 2 xy 2)
      curAreas.write(areas)
      areas.zipWithIndex.foreach { case (area, index) =>
        regions.lift(index).foreach { region =>
          region.fixedW(area.size.x).fixedH(area.size.y)
          parent.assignPosition(region, area.position)
        }
      }
      regions.drop(areas.size).foreach { region =>
        region.fixedW(0.0).fixedH(0.0)
        parent.assignPosition(region, Vec2d.Zero)
      }
    }

    boxContext.root.sub(parent.sub(region(fillId).fillBoth() :: regions: _*))

    //    val Structure = BoxClass()
    //    val BoxList = BoxClass()
    //
    //    val headerId = BoxId()
    //    val menuId = BoxId()
    //    val contentId = BoxId()
    //    val submenuId = BoxId()
    //    val footerId = BoxId()
    //
    //    implicit val styles: Styler = StyleSheet(
    //      isRegion && (headerId || footerId) |> (
    //        _.fillColor(Colors.GreenDarkest),
    //        ),
    //      isRegion && (menuId || submenuId) |> (
    //        _.fillColor(Colors.GreenDark),
    //        ),
    //      isRegion && contentId |> (
    //        _.fillColor(Colors.Green),
    //        ),
    //      isRegion && Structure |> (
    //        _.pad(20 xy 20),
    //        ),
    //      isHBox && BoxList |> (
    //        _.spacingX(10.0),
    //        ),
    //      isVBox && BoxList |> (
    //        _.spacingY(10.0),
    //        ),
    //      isButton && hasAbsParent(headerId || footerId) |> (
    //        _.textColor(Colors.PureWhite),
    //        ),
    //      isButton |> (
    //        _.cursor(Cursors.Auto),
    //        _.pad(10.0 xy 10.0),
    //        _.fillColor(Colors.Blue),
    //        _.textSize(10),
    //        _.textFont(RobotoSlab),
    //        _.fillDepth(4.0),
    //        _.childOffset(0 xy -2.0),
    //      ),
    //      isButton && Hover |> (
    //        _.cursor(Cursors.Pointer),
    //        _.fillColor(Colors.Blue.lighter)
    //      ),
    //      isButton && Hover && Drag |> (
    //        _.fillColor(Colors.Blue.darker),
    //        _.fillDepth(-4.0),
    //        _.childOffset(0 xy 4.0),
    //      )
    //    )
    //
    //    boxContext.root.sub(
    //      vbox().fillBoth().sub(
    //        // header
    //        region(headerId).fillX().addClass(Structure).sub(
    //          hbox().addClass(BoxList).sub(
    //            boxButton().onClick(window.alert("foo")).textValue("OCWALK"),
    //            boxButton().textValue("Home"),
    //            boxButton().textValue("Library")
    //          )
    //        ),
    //        hbox().fillBoth().sub(
    //          // menu
    //          region(menuId).fillY().addClass(Structure).sub(
    //            vbox().addClass(BoxList).sub(
    //              boxButton().textValue("Stuff"),
    //              boxButton().textValue("And"),
    //              boxButton().textValue("Things")
    //            )
    //          ),
    //          // content
    //          region(contentId).fillBoth().addClass(Structure).sub(
    //            vbox().addClass(BoxList).sub(
    //              boxButton().textValue("Content 1"),
    //              boxButton().textValue("Content 2"),
    //              boxButton().textValue("Content 3")
    //            )
    //          ),
    //          // submenu
    //          region(submenuId).fillY().addClass(Structure).sub(
    //            vbox().addClass(BoxList).sub(
    //              boxButton().textValue("Lorem"),
    //              boxButton().textValue("Ipsum")
    //            )
    //          ),
    //        ),
    //        // footer
    //        region(footerId).fillX().addClass(Structure).sub(
    //          hbox().addClass(BoxList).sub(
    //            boxButton().textValue("Footer"),
    //            boxButton().textValue("Stuff"),
    //            icon()
    //              .mutate(_.iconValue(MaterialDesign.AddComment))
    //              .mutate(_.iconSize(64.0))
    //              .mutate(_.iconColor(Colors.PureWhite))
    //          )
    //        )
    //      )
    //    )

    log.info("loaded test ui")
  }

  def loginDiscord(code: String)(implicit generalConfig: GeneralConfig): Unit = for {
    user <- Future.successful(None) // post[LoginDiscord, User]("/api/discord", LoginDiscord(code))
    _ = log.info(s"logged in as [$user]")
    _ = redirectSilent("/pac", preserveQuery = false)
    _ = startPac("/pac")
  } yield user

}