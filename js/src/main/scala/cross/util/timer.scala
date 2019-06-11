package cross.util

import org.scalajs.dom._

object timer {

  class Timer {
    /** Starts the timer to tick with given tps - ticks per seconds */
    def start(tps: Double, code: () => Unit): Unit = {
      val startTime = System.currentTimeMillis()
      code.apply()
      val elapsedTime = System.currentTimeMillis() - startTime
      val delay = (1000 / tps - elapsedTime) max 0
      window.setTimeout(() => start(tps, code), delay)
    }
  }

}