package cross.subbinary

import cross.binary.{BF, BinaryFormat, ByteList}

import scala.language.implicitConversions

trait defaults {
  /** Reads and writes strings */
  implicit val stringFormat: BF[String] = new BinaryFormat[String] {
    override def read(bytes: ByteList): (String, ByteList) = bytes.readString

    override def append(a: String, bytes: ByteList): ByteList = bytes + a

    override def isDefinedFor(a: Any): Boolean = a match {
      case m: String => true
      case o => false
    }
  }

  /** Reads and writes ints */
  implicit val intFormat: BF[Int] = new BinaryFormat[Int] {
    override def read(bytes: ByteList): (Int, ByteList) = bytes.readInt

    override def append(a: Int, bytes: ByteList): ByteList = bytes + a

    override def isDefinedFor(a: Any): Boolean = a match {
      case m: Int => true
      case o => false
    }
  }

  /** Reads and writes booleans */
  implicit val booleanFormat: BF[Boolean] = new BinaryFormat[Boolean] {
    override def read(bytes: ByteList): (Boolean, ByteList) = bytes.readBoolean

    override def append(a: Boolean, bytes: ByteList): ByteList = bytes + a

    override def isDefinedFor(a: Any): Boolean = a match {
      case m: Boolean => true
      case o => false
    }
  }

  /** Reads and writes doubles */
  implicit val doubleFormat: BF[Double] = new BinaryFormat[Double] {
    override def read(bytes: ByteList): (Double, ByteList) = bytes.readDouble

    override def append(a: Double, bytes: ByteList): ByteList = bytes + a

    override def isDefinedFor(a: Any): Boolean = a match {
      case m: Double => true
      case o => false
    }
  }

  /** Reads and writes longs */
  implicit val longFormat: BF[Long] = new BinaryFormat[Long] {
    override def read(bytes: ByteList): (Long, ByteList) = bytes.readLong

    override def append(a: Long, bytes: ByteList): ByteList = bytes + a

    override def isDefinedFor(a: Any): Boolean = a match {
      case m: Long => true
      case o => false
    }
  }
}

object defaults extends defaults {

  implicit class BinaryAnyOps[A: BF](val any: A) {
    /** Converts the value to binary format */
    def toBinary(implicit format: BF[A]): ByteList = format.append(any, ByteList.empty)
  }

}