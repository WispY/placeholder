package cross

import cross.format._

import scala.concurrent.duration._

object config {
  /** Global configuration reader */
  private var globalReader: ConfigReader = _

  /** Defines the global reader */
  def setGlobalReader(reader: ConfigReader): Unit = globalReader = reader

  /** Reads the config keys from runtime environment */
  trait ConfigReader {
    def get(path: Path): Option[String]
  }

  /** Reads the config keys from java runtime environment */
  object JvmReader extends ConfigReader {
    override def get(path: Path): Option[String] = {
      val full = path.stringify
      sys.env.get(full).orElse(sys.props.get(full))
    }
  }

  type Config = List[(Path, String)]
  type ConfigFormat[A] = AbstractFormat[A, Config]
  type CF[A] = ConfigFormat[A]

  /** Configures the application using given default values and overrides from the reader */
  def configureNamespace[A](namespace: String, default: Option[A] = None)(implicit format: CF[A]): A = {
    val path = FieldPathSegment(namespace) :: Nil
    configurePath(path, default)
  }

  /** Configures the application using given default values and overrides from the reader */
  def configurePath[A](path: Path, default: Option[A] = None)(implicit format: CF[A]): A = {
    val defaultConfig = default.map(d => format.append(path, d, Nil)).getOrElse(Nil)
    format.read(path, defaultConfig)._1
  }

  implicit class ConfigOps(val formatted: Config) extends AnyVal {
    /** Converts the config to scala format */
    def toScala[A: CF](path: Path = Nil)(implicit format: CF[A]): A = format.read(path, formatted)._1
  }

  /** Provides the unit for config format */
  implicit val configType: FormatType[Config] = new FormatType[Config] {
    override def unit: Config = Nil
  }

  /** Reads strings */
  implicit val stringFormat: CF[String] = new CF[String] {
    override def read(path: Path, formatted: Config): (String, Config) = {
      val default = formatted.collectFirst { case (p, v) if p == path => v }
      val value = globalReader.get(path).orElse(default).getOrElse(error("missing string", path))
      value -> formatted
    }

    override def append(path: Path, a: String, formatted: Config): Config = {
      val value = path -> a
      formatted :+ value
    }
  }

  /** Reads ints */
  implicit val intFormat: CF[Int] = stringFormat.map(v => v.toInt, v => v.toString)

  /** Reads doubles */
  implicit val doubleFormat: CF[Double] = stringFormat.map(v => v.toDouble, v => v.toString)

  /** Reads finite durations */
  implicit val durationFormat: CF[FiniteDuration] = stringFormat.map(
    { string =>
      val total = "([0-9]+)(d|h|ms|s|m)".r.findAllMatchIn(string).foldLeft(0L) { case (sum, part) =>
        val amount = part.group(1).toInt
        val duration = part.group(2) match {
          case "d" => amount.days
          case "h" => amount.hours
          case "m" => amount.minutes
          case "s" => amount.seconds
          case "ms" => amount.millis
        }
        sum + duration.toMillis
      }
      total.millis
    },
    { duration =>
      val (ignored, stringified) = List(
        1.day -> "d",
        1.hour -> "h",
        1.minute -> "m",
        1.second -> "s",
        1.millis -> "ms"
      ).foldLeft(duration, "") { case ((left, string), (unitDuration, unitName)) =>
        val amount = left.toMillis / unitDuration.toMillis
        if (amount > 0) {
          (left - amount * unitDuration, s"$string$amount$unitName")
        } else {
          (left, string)
        }
      }
      stringified
    }
  )

  /** Reads lists of A */
  implicit def listFormat[A: CF]: CF[List[A]] = new ConfigFormat[List[A]] {
    override def read(path: Path, formatted: Config): (List[A], Config) = {
      globalReader.get(path) match {
        case Some("[]") => Nil -> formatted
        case Some(other) => error(s"wrong list format: $other", path)
        case None =>
          val list = Stream
            .from(0)
            .takeWhile { i =>
              val fullPath = path :+ ArrayPathSegment(i)
              globalReader.get(fullPath).isDefined || formatted.exists { case (p, s) => p == fullPath }
            }
            .map(i => formatted.toScala[A](path :+ ArrayPathSegment(i)))
            .toList
          list -> formatted
      }
    }

    override def append(path: Path, a: List[A], formatted: Config): Config = {
      a.zipWithIndex.foldLeft(formatted) { case (current, (element, index)) =>
        current ++ element.format(path :+ ArrayPathSegment(index))
      }
    }
  }

  /** Reads optional A */
  implicit def optionFormat[A: CF]: CF[Option[A]] = listFormat[A].map(list => list.headOption, option => option.toList)

  private def error(message: String, path: Path, cause: Option[Throwable] = None): Nothing = {
    throw new IllegalArgumentException(s"$message: ${path.stringify}", cause.orNull)
  }
}