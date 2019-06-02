import java.io.FileWriter

val root = sys.env.getOrElse("PLACEHOLDER_ROOT", throw new IllegalStateException("PLACEHOLDER_ROOT env not found"))
val outputFile = s"$root/macros/src/main/scala/cross/subconfig/formats.scala"
val count = 22
val output =
  s"""
package cross.subconfig

import cross.config._
import cross.subconfig.macrodef._

import scala.languageFeature.experimental.macros
import scala.reflect.macros.blackbox

/** Generated. Macros that build config formats for case classes of any size */
object formats {
  implicit val macros: macros = scala.language.experimental.macros

${
    (0 to count).map { size =>
      val list = (0 until size).map(i => s"P${i + 1}")
      val types = (list :+ "A").mkString(", ")
      val constructor = list.mkString(", ")
      val implicits = if (list.isEmpty) "" else s"(implicit ${list.map(t => s"${t.toLowerCase}: CF[$t]").mkString(", ")})"
      s"  def configFormat$size[$types](constructor: ($constructor) => A)$implicits: CF[A] = macro format$size[A]"
    }.mkString("\n\n")
  }

${
    (0 to count).map { size =>
      val list = (0 until size).map(i => s"p${i + 1}")
      val params = if (list.isEmpty) "" else s"(${list.map(t => s"$t: c.Tree").mkString(", ")})"
      val args = (list :+ "Nil").mkString(" :: ")
      s"  def format$size[A: c.WeakTypeTag](c: blackbox.Context)(constructor: c.Tree)$params: c.Expr[CF[A]] = formatX(c)(constructor, $args)"
    }.mkString("\n\n")
  }
}
  """

println(output)

val writer = new FileWriter(outputFile)
writer.write(output)
writer.close()