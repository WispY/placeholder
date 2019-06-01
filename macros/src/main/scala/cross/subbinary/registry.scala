package cross.subbinary

import cross.binary.{BF, ByteList}

object registry {

  /** Indexes the formats with ids to be able to read any message */
  class Registry[A](formats: List[BF[_ <: A]]) {
    private val indexed = formats.zipWithIndex

    /** Writes the message into the byte list marking it with id from registry */
    def write(value: A): ByteList = {
      indexed.find { case (format, index) => format.isDefinedFor(value) } match {
        case Some((format, index)) =>
          format.asInstanceOf[BF[A]].append(value, ByteList.empty + index)
        case None =>
          throw new IllegalArgumentException(s"no format is defined for value: $value")
      }
    }

    /** Reads the message from the byte list used the id prepended to message */
    def read(buffer: ByteList): A = {
      val (id, tail) = buffer.readInt
      formats.lift(id) match {
        case Some(format) =>
          format.read(tail).asInstanceOf[A]
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