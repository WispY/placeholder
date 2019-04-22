package cross

import org.scalajs.dom

object Main {
  def main(args: Array[String]): Unit = {
    val lib = new MyLibrary
    dom.console.log(lib.sq(2))
  }
}