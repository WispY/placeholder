import java.awt.Color
import java.io.{File, FileWriter}

import javax.imageio.ImageIO

val root = sys.env.getOrElse("PLACEHOLDER_ROOT", throw new IllegalStateException("PLACEHOLDER_ROOT env not found"))
val inputFolder = s"$root/out/image"
val treeOutputFile = s"$root/js/src/main/scala/cross/asset/tree.scala"
val uiOutputFile = s"$root/js/src/main/scala/cross/asset/ui.scala"
val flowerOutputFile = s"$root/js/src/main/scala/cross/asset/flower.scala"

val images = new File(inputFolder).listFiles().toList
val treePattern = raw"([0-9])(S|L|R)([+-][0-9])-1.png".r
val assetPattern = raw"(asset-.*).png".r
val flowerPattern = raw"flower-([0-9])-([0-9]).png".r
val rootColor = (172, 50, 50)
val branchColor = (106, 190, 48)
val flowerColor = (91, 110, 225)

case class Info(path: String, level: String, variation: String, rotation: String, root: (Int, Int), branch: (Int, Int), flowers: List[(Int, Int)])

val branches = images.map(file => file -> file.getName).collect {
  case (file, treePattern(level, variation, rotation)) =>
    val image = ImageIO.read(new File(s"$root/out/image/$level$variation$rotation-2.png"))
    val pixels = (0 until image.getWidth).flatMap { x =>
      (0 until image.getHeight).flatMap { y =>
        val color = new Color(image.getRGB(x, y), true)
        val tuple = (color.getRed, color.getGreen, color.getBlue)
        if (color.getAlpha < 128) None else Some((x, y) -> tuple)
      }
    }
    Info(
      path = s"/image/${file.getName}",
      level, variation, rotation,
      root = pixels.collectFirst { case ((x, y), color) if color == rootColor => (x, y) }.get,
      branch = pixels.collectFirst { case ((x, y), color) if color == branchColor => (x, y) }.get,
      flowers = pixels.collect { case ((x, y), color) if color == flowerColor => (x, y) }.toList
    )
}

val branchesDefs = branches.map {
  case Info(path, level, variation, rotation, (rx, ry), (bx, by), flowers) =>
    val variationKey = variation match {
      case "S" => "Straight"
      case "L" => "Left____"
      case "R" => "Right___"
    }
    val flowersString = flowers.map { case (fx, fy) => s"$fx xy $fy" }.mkString(", ")
    s"""    TreeAsset(asset = Asset("$path"), level = $level, rotation = $rotation, variation = $variationKey, rootAnchor = $rx xy $ry, branchAnchor = $bx xy $by, flowerAnchors = List($flowersString))"""
}

val assets = images.map(file => file.getName).collect {
  case assetPattern(name) => s"`$name`" -> s"""  val `$name` = Asset("/image/$name.png")"""
}

val flowers = images.map(file => file -> file.getName).collect {
  case (file, flowerPattern(index, frame)) =>
    (file, index, frame)
}.groupBy(_._2).values.toList.map { frames =>
  val list = frames.sortBy(_._3).map { case (file, index, frame) =>
    val path = s"/image/${file.getName}"
    s"""Asset("$path")"""
  }
  s"""List(${list.mkString(", ")})"""
}

val treeOutput =
  s"""
package cross.asset

import cross.mvc.TreeVariations._
import cross.mvc.{Asset, TreeAsset}
import cross.vec._

object tree {
  val branches = List(
${branchesDefs.mkString(",\n")}
  )
}
"""

val assetOutput =
  s"""
package cross.asset

import cross.mvc.Asset

object ui {
${assets.map(_._2).mkString("\n")}
  val all = List(${assets.map(_._1).mkString(", ")})
}
"""

val flowerOutput =
  s"""
package cross.asset

import cross.mvc.Asset

object flower {
  val flowers = List(
${flowers.mkString(",\n")}
  )
}
"""

println(treeOutput)
println(assetOutput)

val treeWriter = new FileWriter(treeOutputFile)
treeWriter.write(treeOutput)
treeWriter.close()

val uiWriter = new FileWriter(uiOutputFile)
uiWriter.write(assetOutput)
uiWriter.close()

val flowerWriter = new FileWriter(flowerOutputFile)
flowerWriter.write(flowerOutput)
flowerWriter.close()