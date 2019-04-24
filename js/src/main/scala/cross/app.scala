package cross

import cross.global.GlobalContext
import cross.mvc._
import cross.pixi.{ScaleModes, Settings}

/** Starts the UI application */
object app extends App with GlobalContext {
  Settings.SCALE_MODE = ScaleModes.NEAREST
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