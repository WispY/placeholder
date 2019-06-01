package cross

import scala.util.control.NonFatal

/** Configures the application */
object config {

  /** Reads the config keys from runtime environment */
  trait ConfigReader {
    def get(path: List[String]): Option[String]
  }

  /** Reads the config keys from java runtime environment */
  object JvmReader extends ConfigReader {
    override def get(path: List[String]): Option[String] = {
      val full = path.mkString(".")
      sys.env.get(full).orElse(sys.props.get(full))
    }
  }

  /** Formats the config to type A read via provided readed */
  trait ConfigFormat[A] {
    /** Reads the value A at given path */
    def read(path: List[String], default: Option[A])(implicit reader: ConfigReader): A

    /** Maps the current format into a new format using constructor and destructor */
    def map[B](constructor: A => B, destructor: B => A): CF[B] = {
      val delegate = this
      new CF[B] {
        override def read(path: List[String], default: Option[B])(implicit reader: ConfigReader): B = try {
          constructor.apply(delegate.read(path, default.map(destructor)))
        } catch {
          case NonFatal(up) => error("failed to read", path, Some(up))
        }
      }
    }

    /** Produces config reading exception with given message and path */
    def error(message: String, path: List[String], cause: Option[Throwable] = None): Nothing = {
      throw new RuntimeException(s"$message: ${path.mkString(".")}", cause.orNull)
    }
  }

  type CF[A] = ConfigFormat[A]

  /** Configures the application using given default values and overrides from the reader */
  def configureNamespace[A](namespace: String, default: Option[A] = None)(implicit format: CF[A], reader: ConfigReader): A = format.read(namespace :: Nil, default)

  /** Configures the application using given default values and overrides from the reader */
  def configurePath[A](path: List[String], default: Option[A] = None)(implicit format: CF[A], reader: ConfigReader): A = format.read(path, default)

  /** Reads lists of A */
  implicit def listFormat[A: CF]: CF[List[A]] = new ConfigFormat[List[A]] {
    override def read(path: List[String], default: Option[List[A]])(implicit reader: ConfigReader): List[A] = {
      reader.get(path) match {
        case Some("[]") => Nil
        case Some(other) => error(s"wrong list format: $other", path)
        case None =>
          val list = Stream
            .from(0)
            .takeWhile(i => reader.get(path :+ i.toString).isDefined)
            .map(i => configurePath[A](path :+ i.toString, None))
            .toList
          if (list.isEmpty) default.getOrElse(error("missing list", path)) else list
      }
    }
  }

  /** Reads optional A */
  implicit def optionFormat[A: CF]: CF[Option[A]] = listFormat[A].map(list => list.headOption, option => option.toList)

}