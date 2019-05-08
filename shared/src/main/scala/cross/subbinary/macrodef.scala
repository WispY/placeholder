package cross.subbinary

import cross.binary.BF

import scala.languageFeature.experimental.macros
import scala.reflect.macros.blackbox

/** Macros that build the binary format bodies of any size */
object macrodef {
  implicit val macros: macros = scala.language.experimental.macros

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
        val name = TermName(field.fullName.split('.').last)
        (tail: c.Tree) => q"val $nextBytes = $part.append(a.$name, $currentBytes); $tail"
      }
      val lastBytes = TermName(s"bytes${parts.size}")
      val lastLine = (tail: c.Tree) => q"$lastBytes"
      mkblock(c)(lines :+ lastLine)
    }
    c.Expr[BF[A]] {
      q"""
      import cross.binary.ByteList
      new cross.binary.BinaryFormat[$tpe] {
        def read(bytes0: ByteList): ($tpe, ByteList) = { $read }
        def append(a: $tpe, bytes0: ByteList): ByteList = { $write }
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