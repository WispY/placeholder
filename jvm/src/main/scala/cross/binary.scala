package cross

import akka.util.ByteString
import cross.subbinary.defaults
import cross.subbinary.defaults._

object binary extends defaults {

  /** Reads and writes the message A using byte strings */
  trait BinaryFormat[A] {
    /** Reads the message A from buffer and advances it's position */
    def read(bytes: ByteString): (A, ByteString)

    /** Writes the message A to the bytes string advancing it's position */
    def append(a: A, bytes: ByteString): ByteString
  }

  object BinaryFormat {
    /** Builds format from two functions */
    def apply[A](r: ByteString => (A, ByteString), w: (A, ByteString) => ByteString): BinaryFormat[A] = new BinaryFormat[A] {
      override def read(bytes: ByteString): (A, ByteString) = r.apply(bytes)

      override def append(a: A, bytes: ByteString): ByteString = w.apply(a, bytes)
    }
  }

  type BF[A] = BinaryFormat[A]

  /** Reads and writes lists of A */
  implicit def listFormat[A: BF]: BF[List[A]] = new BinaryFormat[List[A]] {
    override def read(bytes: ByteString): (List[A], ByteString) = {
      val (length, tail) = bytes.readInt
      (0 until length).foldLeft[(List[A], ByteString)](Nil, tail) { case ((list, currentTail), index) =>
        val (a, nextTail) = currentTail.toScala[A]
        (list :+ a, nextTail)
      }
    }

    override def append(a: List[A], bytes: ByteString): ByteString = {
      val next = bytes.appendInt(a.size)
      a.foldLeft(next) { case (current, element) =>
        current ++ element.toBinary
      }
    }
  }

}