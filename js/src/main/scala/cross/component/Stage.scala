package cross.component

import cross.util.animation.Animation

import scala.concurrent.Future

/** Represents a screen of the game */
trait Stage extends Component {
  /** Creates all of the UI element of the stage */
  def create(): Future[Unit]

  /** Animates all UI elements to stage display state */
  def fadeIn(): Animation

  /** Puts all UI elements into stage display state and animates them to fade out state */
  def fadeOut(): Animation
}