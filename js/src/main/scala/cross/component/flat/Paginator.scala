package cross.component.flat

import cross.common.{Data, Vec2d, Writeable}
import cross.component.flat.Button.ButtonStyle
import cross.component.flat.Paginator.PaginatorStyle
import cross.component.util.FontStyle
import cross.layout._
import cross.ops._
import cross.util.logging.Debug

/** Controls the pagination over the given list of elements */
class Paginator[A](style: PaginatorStyle, source: Data[List[A]], view: Writeable[List[A]]) extends StackBox with Debug {
  private val first = button(style.button).children(label("<<", style.buttonLabel))
  private val previous = button(style.button).children(label("<", style.buttonLabel))
  private val current = label("", style.pageLabel)
  private val next = button(style.button).children(label(">", style.buttonLabel))
  private val last = button(style.button).children(label(">>", style.buttonLabel))

  private val currentIndex: Writeable[Int] = Data(0)
  private val totalSize: Writeable[Int] = Data(0)

  this.init()

  private def init(): Unit = {
    source /> { case list =>
      log.info(s"paginator: ${list.size}")
      totalSize.write(list.size)
      currentIndex.write(0)
    }
    (currentIndex && totalSize) /> { case (startIndex, total) =>
      val endIndex = (startIndex + style.pageSize) min total
      if (endIndex > startIndex + 1) {
        current.label(s"${startIndex + 1} - $endIndex / $total")
      } else {
        current.label(s"$endIndex / $total")
      }
      view.write(source().slice(startIndex, endIndex))
    }
    first.onClick { _ => paginate(-totalSize()) }
    previous.onClick { _ => paginate(-style.pageSize) }
    next.onClick { _ => paginate(style.pageSize) }
    last.onClick { _ => paginate(totalSize()) }

    this.fillX.children(
      xbox.space(style.space).children(
        first.pad(style.pad),
        previous.pad(style.pad),
        sbox.pad(style.pad).fillX.children(current),
        next.pad(style.pad),
        last.pad(style.pad)
      )
    )
  }

  /** Shifts the pagination by the given offset */
  private def paginate(offset: Int): Unit = {
    val lastIndex = totalSize() - 1
    val maxPageIndex = lastIndex - (lastIndex % style.pageSize)
    val target = ((currentIndex() + offset) max 0) min maxPageIndex
    currentIndex.write(target)
  }
}

object Paginator {

  /** Describes the style of the pagination bar */
  case class PaginatorStyle(pageSize: Int,
                            space: Vec2d,
                            pad: Vec2d,
                            button: ButtonStyle,
                            buttonLabel: FontStyle,
                            pageLabel: FontStyle)

}