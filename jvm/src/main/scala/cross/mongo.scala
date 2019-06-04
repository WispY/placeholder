package cross

import cross.format._
import org.mongodb.scala.bson._

import scala.collection.JavaConverters._

object mongo {

  type MongoFormat[A] = AbstractFormat[A, BsonValue]
  type MF[A] = MongoFormat[A]

  implicit class DocumentOps(val document: Document) extends AnyVal {
    /** Converts bson document to scala object */
    def asScala[A: MF](implicit format: MF[A]): A = format.read(Nil, document.toBsonDocument)._1
  }

  implicit class BsonValueOps(val bson: BsonValue) extends AnyVal {
    /** Converts bson value to scala object */
    def asScala[A: MF](implicit format: MF[A]): A = format.read(Nil, bson)._1
  }

  implicit class MongoAnyRefOps[A <: AnyRef](val any: A) extends AnyVal {
    /** Converts scala value to bson document */
    def asBson(implicit format: MF[A]): Document = Document(any.format().asDocument())
  }

  /** Provides the unit for mongo format */
  implicit val mongoType: FormatType[BsonValue] = new FormatType[BsonValue] {
    override def unit: BsonValue = new BsonDocument()
  }

  /** Reads and writes bsons */
  implicit val bsonValueFormat: MF[BsonValue] = new MF[BsonValue] {
    override def read(path: Path, formatted: BsonValue): (BsonValue, BsonValue) = {
      readPath(path, formatted).getOrElse(throw new IllegalArgumentException(s"missing field: ${path.stringify}")) -> formatted
    }

    override def append(path: Path, a: BsonValue, formatted: BsonValue): BsonValue = {
      writePath(path, formatted, a)
      formatted
    }
  }

  /** Reads and writes strings */
  implicit val stringFormat: MF[String] = bsonValueFormat.map(
    {
      case string: BsonString => string.getValue
      case other => throw new IllegalArgumentException(s"wrong string type: $other")
    },
    string => new BsonString(string)
  )

  /** Reads and writes booleans */
  implicit val booleanFormat: MF[Boolean] = bsonValueFormat.map(
    {
      case boolean: BsonBoolean => boolean.getValue
      case other => throw new IllegalArgumentException(s"wrong boolean type: $other")
    },
    ooolean => new BsonBoolean(ooolean)
  )

  /** Reads and writes ints */
  implicit val intFormat: MF[Int] = bsonValueFormat.map(
    {
      case int: BsonInt32 => int.getValue
      case other => throw new IllegalArgumentException(s"wrong int type: $other")
    },
    int => new BsonInt32(int)
  )

  /** Reads and writes longs */
  implicit val longFormat: MF[Long] = bsonValueFormat.map(
    {
      case long: BsonInt64 => long.getValue
      case other => throw new IllegalArgumentException(s"wrong long type: $other")
    },
    long => new BsonInt64(long)
  )

  /** Reads lists of A */
  implicit def listFormat[A: MF]: MF[List[A]] = new MF[List[A]] {
    override def read(path: Path, formatted: BsonValue): (List[A], BsonValue) = {
      readPath(path, formatted) match {
        case None =>
          Nil -> formatted
        case Some(array: BsonArray) =>
          array.getValues.asScala.toList.map(element => element.asScala[A]) -> formatted
        case Some(other) =>
          throw new IllegalArgumentException(s"wrong list format: $other")
      }
    }

    override def append(path: Path, a: List[A], formatted: BsonValue): BsonValue = {
      a.zipWithIndex.foreach { case (element, index) =>
        element.format(path :+ ArrayPathSegment(index), formatted)
      }
      formatted
    }
  }

  /** Reads optional A */
  implicit def optionFormat[A: MF]: MF[Option[A]] = listFormat[A].map(list => list.headOption, option => option.toList)

  /** Reads the bson value at given path */
  private def readPath(path: Path, bson: BsonValue): Option[BsonValue] = path match {
    case Nil =>
      Some(bson)

    case FieldPathSegment(field) :: tail =>
      Option(bson.asDocument().get(field)).flatMap(b => readPath(tail, b))

    case ArrayPathSegment(index) :: tail =>
      val array = bson.asArray()
      if (index >= 0 && index < array.size()) readPath(tail, array.get(index))
      else None
  }

  /** Writes the bson value at given path */
  private def writePath(path: Path, bson: BsonValue, value: BsonValue): Unit = path match {
    case Nil =>
      throw new IllegalArgumentException(s"cannot write value at empty path: $bson")

    case FieldPathSegment(field) :: Nil =>
      bson.asDocument().put(field, value)

    case FieldPathSegment(field) :: tail =>
      Option(bson.asDocument().get(field)) match {
        case Some(sub) =>
          writePath(tail, sub, value)
        case None =>
          val sub = tail.head match {
            case _: FieldPathSegment => new BsonDocument()
            case _: ArrayPathSegment => new BsonArray()
          }
          bson.asDocument().put(field, sub)
          writePath(tail, sub, value)
      }

    case ArrayPathSegment(index) :: Nil =>
      bson.asArray().add(value)

    case ArrayPathSegment(index) :: tail =>
      if (index >= 0 && index < bson.asArray().size()) {
        writePath(tail, bson.asArray().get(index), value)
      } else {
        val sub = tail.head match {
          case _: FieldPathSegment => new BsonDocument()
          case _: ArrayPathSegment => new BsonArray()
        }
        bson.asArray().add(sub)
        writePath(tail, sub, value)
      }
  }

}