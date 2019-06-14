package cross.pac

import cross.common._
import cross.util.global.GlobalContext
import cross.util.logging.Logging
import cross.util.mvc.{GenericController, GenericModel}
import cross.util.timer.Timer

import scala.concurrent.Future

object mvc extends GlobalContext with Logging {

  /** Contains logic for pac project */
  class Controller(val model: Model) extends GenericController[Stages.Value] {
    val timer = new Timer()

    /** Initializes the controller */
    def start(): Future[Unit] = Future {
      log.info("[controller] starting...")
      timer.start(60, () => model.tick.write(model.tick() + 1))
      // bind()
      log.info("[controller] started")
    }

    /** Updates the rendering screen size */
    override def setScreenSize(size: Vec2i): Unit = model.screen.write(size)

    /** Updates the global mouse position on the screen */
    override def setMousePosition(mouse: Vec2d): Unit = model.mouse.write(mouse)
  }

  /** Contains data for pac project */
  case class Model(tick: Writeable[Long] = Data(0),
                   screen: Writeable[Vec2i] = Data(0 xy 0),
                   scale: Writeable[Double] = Data(1.0),
                   stage: Writeable[Stages.Value] = Data(Stages.ArtChallenges),
                   mouse: Writeable[Vec2d] = Data(Vec2d.Zero)) extends GenericModel[Stages.Value]

  /** Stages for pac project */
  object Stages extends Enumeration {
    val ArtChallenges, GalleryView, SubmissionView = Value
  }

}