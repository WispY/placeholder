package cross

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8

import cross.subbinary.defaults
import cross.subbinary.defaults._
import cross.common._

object binary extends defaults {

  /** Reads and writes the message A using byte strings */
  trait BinaryFormat[A] {
    /** Reads the message A from buffer and advances it's position */
    def read(bytes: ByteList): (A, ByteList)

    /** Writes the message A to the bytes string advancing it's position */
    def append(a: A, bytes: ByteList): ByteList

    /** Tells whether or now the format supports the given value */
    def isDefinedFor(a: Any): Boolean = false

    /** Creates a new binary format based on this one and type mapping */
    def map[B](constructor: A => B, destructor: B => A): BF[B] = {
      val delegate = this
      new BF[B] {
        override def read(bytes: ByteList): (B, ByteList) = delegate.read(bytes).chain { case (a, tail) => constructor.apply(a) -> tail }

        override def append(a: B, bytes: ByteList): ByteList = delegate.append(destructor.apply(a), bytes)
      }
    }
  }

  type BF[A] = BinaryFormat[A]

  /** Reads and writes lists of A */
  implicit def listFormat[A: BF]: BF[List[A]] = new BinaryFormat[List[A]] {
    override def read(bytes: ByteList): (List[A], ByteList) = {
      val (length, tail) = bytes.readInt
      (0 until length).foldLeft[(List[A], ByteList)](Nil, tail) { case ((list, currentTail), index) =>
        val (a, nextTail) = currentTail.toScala[A]
        (list :+ a, nextTail)
      }
    }

    override def append(a: List[A], bytes: ByteList): ByteList = {
      val next = bytes + a.size
      a.foldLeft(next) { case (current, element) =>
        current + element.toBinary
      }
    }
  }

  /** Reads and writes optional A */
  implicit def optionFormat[A: BF]: BF[Option[A]] = listFormat[A].map(list => list.headOption, option => option.toList)

  /** Represents a lazy byte array */
  case class ByteList(parts: List[ByteBuffer]) {
    /** Appends part */
    def +(part: ByteBuffer) = ByteList(parts :+ part)

    /** Appends boolean */
    def +(boolean: Boolean): ByteList = {
      val byte: Byte = if (boolean) 1 else 0
      this + ByteBuffer.allocate(1).put(byte)
    }

    /** Appends integer */
    def +(int: Int): ByteList = this + ByteBuffer.allocate(4).putInt(int)

    /** Appends long */
    def +(long: Long): ByteList = this + ByteBuffer.allocate(8).putLong(long)

    /** Appends double */
    def +(double: Double): ByteList = this + ByteBuffer.allocate(8).putDouble(double)

    /** Appends string */
    def +(string: String): ByteList = {
      this + string.length + ByteBuffer.wrap(string.getBytes(UTF_8))
    }

    /** Appends another byte list */
    def +(other: ByteList): ByteList = copy(parts = parts ++ other.parts)

    /** Concatenates the byte list into at most one buffer */
    def compact: ByteBuffer = {
      if (parts.size > 1) {
        val total = parts.map(p => p.limit()).sum
        val buffer = ByteBuffer.allocate(total)
        parts.foreach { p =>
          p.rewind()
          buffer.put(p)
        }
        buffer.rewind()
        buffer
      } else {
        parts.headOption match {
          case Some(buffer) =>
            buffer.rewind()
            buffer
          case None =>
            ByteList.emptyBuffer
        }
      }
    }

    /** Reads the boolean from byte list */
    def readBoolean: (Boolean, ByteList) = {
      val buffer = this.compact
      (buffer.get() == 1, remaining(buffer))
    }

    /** Reads the integer from the byte list */
    def readInt: (Int, ByteList) = {
      val buffer = this.compact
      (buffer.getInt(), remaining(buffer))
    }

    /** Reads the long from the byte list */
    def readLong: (Long, ByteList) = {
      val buffer = this.compact
      (buffer.getLong(), remaining(buffer))
    }

    /** Reads the double from the byte list */
    def readDouble: (Double, ByteList) = {
      val buffer = this.compact
      (buffer.getDouble(), remaining(buffer))
    }

    /** Reads the string from the byte list */
    def readString: (String, ByteList) = {
      val (size, tail) = readInt
      val stringBuffer = new Array[Byte](size)
      val buffer = tail.compact
      buffer.get(stringBuffer)
      (new String(stringBuffer, UTF_8), remaining(buffer))
    }

    /** Converts remaining bytes from the buffer into a new byte list */
    private def remaining(buffer: ByteBuffer): ByteList = {
      if (buffer.hasRemaining) {
        ByteList(buffer.slice() :: Nil)
      } else {
        ByteList.empty
      }
    }

    /** Converts the value to scala format */
    def toScala[A: BF](implicit format: BF[A]): (A, ByteList) = format.read(this)
  }

  object ByteList {
    val empty: ByteList = ByteList(Nil)

    val emptyBuffer: ByteBuffer = ByteBuffer.allocate(0)
  }

}