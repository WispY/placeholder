package cross

import cross.common._
import cross.component.util._
import cross.general.config.{GeneralConfig, JsReader}
import cross.pixi.{ScaleModes, Settings}
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

  def startPac(path: String): Unit = {
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
      _ <- fonts.load(Roboto :: RobotoSlab :: Nil)
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

    val Structure = BoxClass()
    val BoxList = BoxClass()

    val headerId = BoxId()
    val menuId = BoxId()
    val contentId = BoxId()
    val submenuId = BoxId()
    val footerId = BoxId()

    implicit val styles: Styler = StyleSheet(
      isRegion && (headerId || footerId) |> (
        _.fillColor(Colors.GreenDarkest)
        ),
      isRegion && (menuId || submenuId) |> (
        _.fillColor(Colors.GreenDark)
        ),
      isRegion && contentId |> (
        _.fillColor(Colors.Green)
        ),
      isRegion && Structure |> (
        _.pad(20 xy 20)
        ),
      isHBox && BoxList |> (
        _.spacingX(10.0)
        ),
      isVBox && BoxList |> (
        _.spacingY(10.0)
        ),
      isText && hasAbsParent(headerId || footerId) |> {
        _.textColor(Colors.PureWhite)
      }
    )

    boxContext.root.withChildren(
      vbox().fillBoth().withChildren(
        // header
        region(headerId).fillX().addClass(Structure).withChildren(
          hbox().addClass(BoxList).withChildren(
            text().textValue("OCWALK"),
            text().textValue("Home"),
            text().textValue("Library")
          )
        ),
        hbox().fillBoth().withChildren(
          // menu
          region(menuId).fillY().addClass(Structure).withChildren(
            vbox().addClass(BoxList).withChildren(
              text().textValue("Stuff"),
              text().textValue("And"),
              text().textValue("Things")
            )
          ),
          // content
          region(contentId).fillBoth().addClass(Structure).withChildren(
            vbox().addClass(BoxList).withChildren(
              text().textValue("Content 1"),
              text().textValue("Content 2"),
              text().textValue("Content 3")
            )
          ),
          // submenu
          region(submenuId).fillY().addClass(Structure).withChildren(
            vbox().addClass(BoxList).withChildren(
              text().textValue("Lorem"),
              text().textValue("Ipsum")
            )
          ),
        ),
        // footer
        region(footerId).fillX().addClass(Structure).withChildren(
          hbox().addClass(BoxList).withChildren(
            text().textValue("Footer"),
            text().textValue("Stuff")
          )
        )
      )
    )

    log.info("loaded test ui")
  }

  def loginDiscord(code: String): Unit = for {
    user <- Future.successful(None) // post[LoginDiscord, User]("/api/discord", LoginDiscord(code))
    _ = log.info(s"logged in as [$user]")
    _ = redirectSilent("/pac", preserveQuery = false)
    _ = startPac("/pac")
  } yield user

}