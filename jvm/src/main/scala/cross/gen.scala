package cross

import java.awt.image.BufferedImage
import java.io.File

import cross.box.ImageStyle.Tileset
import cross.common._
import cross.processing.packer
import javax.imageio.ImageIO
import net.coobird.thumbnailator.Thumbnails

/** Generates application resources */
object gen extends App {
  val target = "./out"

  println("Generating tilesets")
  genTileset(cross.ocwalk.style.tileset)

  /** Generates image tileset */
  def genTileset(tileset: Tileset): Unit = {
    println(s"Generating tileset [$tileset]")
    val sources = tileset.images.map(i => i.source).distinct
    println(s"Loading [${sources.size}] source images")
    val images = sources.map(s => s -> ImageIO.read(new File(s"$target${s.path}"))).toMap
    println(s"Processing [${tileset.images.size}] images")
    val processed = tileset.images.map { ref =>
      val image = images(ref.source)
      val size = if (ref.size == Vec2d.Zero) image.getWidth xy image.getHeight else ref.size.toInt
      val out = Thumbnails
        .of(image)
        .size(size.x, size.y)
        .useExifOrientation(false)
        .imageType(BufferedImage.TYPE_INT_ARGB)
        .outputFormat("png")
        .asBufferedImage()
      size.iterate.foreach { case (x, y) =>
        val original = Colors.argb(out.getRGB(x, y))
        val replacement = ref.color.apply(original).copy(a = original.a)
        out.setRGB(x, y, replacement.toArgb)
      }
      ref.copy(size = size.toDouble) -> out
    }
    println(s"Packing [${processed.size}] images")
    val (totalSize, areas) = packer.pack(processed.map { case (ref, image) => ref.size}, 1 xy 1)
    println(s"Rendering tileset")
    val out = new BufferedImage(totalSize.x.toInt, totalSize.y.toInt, BufferedImage.TYPE_INT_ARGB)
    processed.zip(areas).foreach { case ((ref, image), area) =>
      out.getGraphics.drawImage(image, area.position.x.toInt, area.position.y.toInt, null)
    }
    println(s"Saving tileset image at [${tileset.imagePath}]")
    val outFile = new File(s"$target${tileset.imagePath}")
    outFile.getParentFile.mkdirs()
    outFile.createNewFile()
    ImageIO.write(out, "png", outFile)
    println(s"Successfully produced tileset [$tileset]")
  }
}