package cross.subconfig

import cross.config.CF

import scala.languageFeature.experimental.macros
import scala.reflect.macros.blackbox

/** Macros that build the config format bodies of any size */
object macrodef {
  implicit val macros: macros = scala.language.experimental.macros

  def formatX[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree, parts: List[c.Tree]): c.Expr[CF[A]] = {
    import c.universe._
    val tpe = weakTypeOf[A]
    val read = {
      val fields = tpe.decls.collectFirst { case primary: MethodSymbol if primary.isPrimaryConstructor => primary }.get.paramLists.head
      val (lines, names) = parts.zip(fields).zipWithIndex.map { case ((part, field), i) =>
        val value = TermName(s"value$i")
        val fieldRef = TermName(field.fullName.split('.').last)
        val fieldName = field.name.decodedName.toString
        val line = (tail: c.Tree) => q"""val $value = $part.read(path :+ $fieldName, default.map(d => d.$fieldRef))(reader); $tail"""
        line -> q"$value"
      }.unzip
      val lastLine = (tail: c.Tree) => q"new $tpe(..$names)"
      mkblock(c)(lines :+ lastLine)
    }
    c.Expr[CF[A]] {
      q"""
      new cross.config.ConfigFormat[$tpe] {
        def read(path: List[String], default: Option[$tpe])(implicit reader: ConfigReader): $tpe = { $read }
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