package cross.subconfig

import cross.config._

object defaults {
  /** Reads strings */
  implicit val stringFormat: CF[String] = new CF[String] {
    override def read(path: List[String], default: Option[String])(implicit reader: ConfigReader): String = {
      reader.get(path).orElse(default).getOrElse(error("missing string", path))
    }
  }

  /** Reads ints */
  implicit val intFormat: CF[Int] = stringFormat.map(v => v.toInt, v => v.toString)

  /** Reads doubles */
  implicit val doubleFormat: CF[Double] = stringFormat.map(v => v.toDouble, v => v.toString)
}