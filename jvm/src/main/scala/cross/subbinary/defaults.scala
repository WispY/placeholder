package cross.subbinary

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8

import akka.util.ByteString
import cross.binary.{BF, BinaryFormat}
import cross.subbinary.defaults._

import scala.language.implicitConversions

trait defaults {
  /** Reads and writes strings */
  implicit val stringFormat: BF[String] = new BinaryFormat[String] {
    override def read(bytes: ByteString): (String, ByteString) = {
      val (length, tail) = bytes.readInt
      val value = new String(tail.take(length).toArray, UTF_8)
      value -> tail.drop(length)
    }

    override def append(a: String, bytes: ByteString): ByteString = {
      val array = a.getBytes(UTF_8)
      val sum = bytes.appendInt(array.length)
      sum ++ ByteString(array)
    }
  }

  /** Reads and writes ints */
  implicit val intFormat: BF[Int] = new BinaryFormat[Int] {
    override def read(bytes: ByteString): (Int, ByteString) = bytes.readInt

    override def append(a: Int, bytes: ByteString): ByteString = bytes.appendInt(a)
  }

  /** Reads and writes booleans */
  implicit val booleanFormat: BF[Boolean] = new BinaryFormat[Boolean] {
    override def read(bytes: ByteString): (Boolean, ByteString) = {
      (bytes.head == 1, bytes.tail)
    }

    override def append(a: Boolean, bytes: ByteString): ByteString = {
      val byte: Byte = if (a) 1 else 0
      bytes :+ byte
    }
  }

  /** Reads and writes doubles */
  implicit val doubleFormat: BF[Double] = new BinaryFormat[Double] {
    override def read(bytes: ByteString): (Double, ByteString) = {
      val next = bytes.take(8)
      val value = ByteBuffer.wrap(next.toArray).getDouble
      value -> bytes.drop(8)
    }

    override def append(a: Double, bytes: ByteString): ByteString = {
      bytes ++ ByteString(ByteBuffer.allocate(8).putDouble(a).array())
    }
  }

  /** Reads and writes longs */
  implicit val longFormat: BF[Long] = new BinaryFormat[Long] {
    override def read(bytes: ByteString): (Long, ByteString) = {
      val next = bytes.take(8)
      val value = ByteBuffer.wrap(next.toArray).getLong
      value -> bytes.drop(8)
    }

    override def append(a: Long, bytes: ByteString): ByteString = {
      bytes ++ ByteString(ByteBuffer.allocate(8).putLong(a).array())
    }
  }
}

object defaults extends defaults {

  implicit class BinaryByteStringOps(val bytes: ByteString) extends AnyVal {
    /** Reads 4 byte int value from bytes */
    def readInt: (Int, ByteString) = {
      val next = bytes.take(4)
      val value = ByteBuffer.wrap(next.toArray).getInt
      value -> bytes.drop(4)
    }

    /** Appends 4 byte int value to bytes */
    def appendInt(int: Int): ByteString = {
      bytes ++ ByteString(ByteBuffer.allocate(4).putInt(int).array())
    }

    /** Converts the value to scala format */
    def toScala[A: BF](implicit format: BF[A]): (A, ByteString) = format.read(bytes)
  }

  implicit class BinaryAnyOps[A: BF](val any: A) {
    /** Converts the value to binary format */
    def toBinary(implicit format: BF[A]): ByteString = format.append(any, ByteString.empty)
  }

}