package cross

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8

import akka.util.ByteString

import scala.languageFeature.experimental.macros
import scala.reflect.macros.blackbox

object binary {

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

  class Registry(formats: List[BF[_]]) {

  }

  implicit val macros: macros = scala.language.experimental.macros
  implicit val stringFormat: BF[String] = new BinaryFormat[String] {
    override def read(bytes: ByteString): (String, ByteString) = {
      val (length, tail) = bytes.readInt
      val value = new String(bytes.take(length).toArray, UTF_8)
      value -> tail.drop(length)
    }

    override def append(a: String, bytes: ByteString): ByteString = {
      val array = a.getBytes(UTF_8)
      val sum = bytes.appendInt(array.length)
      sum ++ ByteString(array)
    }
  }
  implicit val intFormat: BF[Int] = new BinaryFormat[Int] {
    override def read(bytes: ByteString): (Int, ByteString) = bytes.readInt

    override def append(a: Int, bytes: ByteString): ByteString = bytes.appendInt(a)
  }

  implicit class ByteStringOps(val bytes: ByteString) extends AnyVal {
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
  }

  def binaryFormat2[P1, P2, A](constructor: (P1, P2) => A)(implicit p1: BF[P1], p2: BF[P2]): BF[A] = macro format2[A]

  def format2[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)(p1: c.Tree, p2: c.Tree): c.Expr[BF[A]] = formatX(c)(constructor, p1 :: p2 :: Nil)

  def formatX[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree, parts: List[c.Tree]): c.Expr[BF[A]] = {
    import c.universe._
    val tpe = weakTypeOf[A]
    val read = {
      val (lines, names) = parts.zipWithIndex.map { case (part, i) =>
        val value = TermName(s"value$i")
        val currentBytes = TermName(s"bytes$i")
        val nextBytes = TermName(s"bytes${i + 1}")
        val line = (tail: c.Tree) => q"val ($value, $nextBytes) = $part.read($currentBytes); $tail"
        line -> q"$value"
      }.unzip
      val lastBytes = TermName(s"bytes${parts.size}")
      val lastLine = (tail: c.Tree) => q"new $tpe(..$names) -> $lastBytes"
      mkblock(c)(lines :+ lastLine)
    }
    val write = {
      val fields = tpe.decls.collectFirst { case primary: MethodSymbol if primary.isPrimaryConstructor => primary }.get.paramLists.head
      val lines = parts.zip(fields).zipWithIndex.map { case ((part, field), i) =>
        val currentBytes = TermName(s"bytes$i")
        val nextBytes = TermName(s"bytes${i + 1}")
        (tail: c.Tree) => q"val $nextBytes = $part.append(a.$field, $currentBytes); $tail"
      }
      val lastBytes = TermName(s"bytes${parts.size}")
      val lastLine = (tail: c.Tree) => q"$lastBytes"
      mkblock(c)(lines :+ lastLine)
    }
    c.Expr[BF[A]] {
      q"""
      import akka.util.ByteString
      println(${showCode(read)})
      println(${showCode(write)})
      println(${show(write)})
      println(${showRaw(write)})
      new cross.binary.BinaryFormat[$tpe] {
        def read(bytes0: ByteString): ($tpe, ByteString) = { $read }
        def append(a: $tpe, bytes0: ByteString): ByteString = { $write }
      }
      """
    }
  }

  def mkblock(c: blackbox.Context)(lines: List[c.Tree => c.Tree]): c.Tree = {
    import c.universe._
    lines.foldRight(q"") { case (head, tail) =>
      q"""
      ${head.apply(tail)}
      """
    }
  }

}