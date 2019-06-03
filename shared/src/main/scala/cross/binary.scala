package cross

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8

import cross.format._

object binary {

  /** Reads and writes the message A using byte strings */
  type BinaryFormat[A] = AbstractFormat[A, ByteList]
  type BF[A] = BinaryFormat[A]

  /** Provides the unit for binary format */
  implicit val binaryType: FormatType[ByteList] = new FormatType[ByteList] {
    override def unit: ByteList = ByteList.empty
  }

  /** Reads and writes strings */
  implicit val stringFormat: BF[String] = new BinaryFormat[String] {
    override def read(path: Path, bytes: ByteList): (String, ByteList) = bytes.readString

    override def append(path: Path, a: String, bytes: ByteList): ByteList = bytes + a
  }

  /** Reads and writes ints */
  implicit val intFormat: BF[Int] = new BinaryFormat[Int] {
    override def read(path: Path, bytes: ByteList): (Int, ByteList) = bytes.readInt

    override def append(path: Path, a: Int, bytes: ByteList): ByteList = bytes + a
  }

  /** Reads and writes booleans */
  implicit val booleanFormat: BF[Boolean] = new BinaryFormat[Boolean] {
    override def read(path: Path, bytes: ByteList): (Boolean, ByteList) = bytes.readBoolean

    override def append(path: Path, a: Boolean, bytes: ByteList): ByteList = bytes + a
  }

  /** Reads and writes doubles */
  implicit val doubleFormat: BF[Double] = new BinaryFormat[Double] {
    override def read(path: Path, bytes: ByteList): (Double, ByteList) = bytes.readDouble

    override def append(path: Path, a: Double, bytes: ByteList): ByteList = bytes + a
  }

  /** Reads and writes longs */
  implicit val longFormat: BF[Long] = new BinaryFormat[Long] {
    override def read(path: Path, bytes: ByteList): (Long, ByteList) = bytes.readLong

    override def append(path: Path, a: Long, bytes: ByteList): ByteList = bytes + a
  }

  /** Reads and writes lists of A */
  implicit def listFormat[A: BF]: BF[List[A]] = new BinaryFormat[List[A]] {
    override def read(path: Path, bytes: ByteList): (List[A], ByteList) = {
      val (length, tail) = bytes.readInt
      (0 until length).foldLeft[(List[A], ByteList)](Nil, tail) { case ((list, currentTail), index) =>
        val (a, nextTail) = currentTail.toScala[A](path :+ ArrayPathSegment(index))
        (list :+ a, nextTail)
      }
    }

    override def append(path: Path, a: List[A], bytes: ByteList): ByteList = {
      val next = bytes + a.size
      a.zipWithIndex.foldLeft(next) { case (current, (element, index)) =>
        current + element.format(path :+ ArrayPathSegment(index))
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
    def toScala[A: BF](path: Path = Nil)(implicit format: BF[A]): (A, ByteList) = format.read(path, this)
  }

  object ByteList {
    val empty: ByteList = ByteList(Nil)

    val emptyBuffer: ByteBuffer = ByteBuffer.allocate(0)
  }

  /** Indexes the formats with ids to be able to read any message */
  class Registry[A](formats: List[BF[_ <: A]]) {
    private val indexed = formats.zipWithIndex

    /** Writes the message into the byte list marking it with id from registry */
    def write(value: A): ByteList = {
      indexed.find { case (format, index) => format.isDefinedFor(value) } match {
        case Some((format, index)) =>
          format.asInstanceOf[BF[A]].append(Nil, value, ByteList.empty + index)
        case None =>
          throw new IllegalArgumentException(s"no format is defined for value: $value")
      }
    }

    /** Reads the message from the byte list used the id prepended to message */
    def read(buffer: ByteList): A = {
      val (id, tail) = buffer.readInt
      formats.lift(id) match {
        case Some(format) =>
          format.read(Nil, tail).asInstanceOf[A]
        case None =>
          throw new IllegalArgumentException(s"no format found to read message with id: $id")
      }
    }
  }

  object Registry {
    /** Creates new registry from given list of formats */
    def apply[A](formats: BF[_ <: A]*): Registry[A] = new Registry[A](formats.toList)
  }

}