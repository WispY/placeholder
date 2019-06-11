package cross

import cross.ffo.FontFaceObserver
import cross.pixi.{ScaleModes, Settings}
import cross.sakura.mvc._
import cross.sakura.ui
import cross.util.global.GlobalContext
import cross.util.{animation, global, spring}
import org.scalajs.dom._

import scala.util.{Failure, Success}

/** Starts the UI application */
object app extends App with GlobalContext {
  window.location.pathname match {
    case sakura if sakura.startsWith("/sakura") =>
      startSakura()
    case pac if pac.startsWith("/pac") =>
      startPac()
    case _ =>
      log.info("[app] redirecting to [/pac]")
      window.location.href = "/pac"
  }

  def startSakura(): Unit = {
    log.info("[app] starting sakura project")
    Settings.SCALE_MODE = ScaleModes.NEAREST
    document.title = "Sakura Challenge"
    val model = Model()
    val controller = new Controller(model)

    for {
      ui <- ui.load(controller)
      _ <- spring.load(controller)
      _ <- animation.load(controller)
      _ <- controller.start()
      _ = global.export(
        "app" -> ui.app
      )
    } yield ()
  }

  def startPac(): Unit = {
    log.info("[app] starting pac project")
    document.title = "Poku Art Challenge"
    new FontFaceObserver("Roboto Slab").load().toFuture.onComplete {
      case Success(observer) =>
        log.info("font loaded")
      case Failure(error) =>
        log.error("font failed", error)
    }
  }

}