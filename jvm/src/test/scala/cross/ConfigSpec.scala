package cross

import cross.config._
import cross.format._

class ConfigSpec extends Spec {
  private val recompile = 1

  before {
    sys.props.clear()
    setGlobalReader(JvmReader)
  }

  /** Creates field path from string list */
  def path(list: String*): Path = list.map(s => FieldPathSegment(s)).toList

  "config" can {
    "read present system property" in {
      sys.props.put("foo.bar.baz", "configured")
      configurePath(path("foo", "bar", "baz"), Some("missing")) shouldBe "configured"
    }

    "read missing system property" in {
      configurePath(path("foo", "bar", "baz"), Some("missing")) shouldBe "missing"
    }

    "fail to read missing system property" in {
      intercept[IllegalArgumentException](configurePath[String](path("foo", "bar", "baz"))).getMessage shouldBe "missing string: foo.bar.baz"
    }

    "read different types" in {
      sys.props.put("ns.string", "string")
      sys.props.put("ns.int", "123")
      sys.props.put("ns.double", "1.23")
      configurePath[String](path("ns", "string")) shouldBe "string"
      configurePath[Int](path("ns", "int")) shouldBe 123
      configurePath[Double](path("ns", "double")) shouldBe 1.23

      intercept[IllegalArgumentException](configurePath[Int](path("ns", "string"))).getMessage shouldBe "failed to read: ns.string"
    }

    "read person" in {
      case class Person(name: String, age: Int)
      implicit val personFormat: CF[Person] = format2(Person)
      val sample = Person("John", 23)
      configureNamespace[Person]("person", Some(sample)) shouldBe sample

      sys.props.put("person.name", sample.name)
      sys.props.put("person.age", sample.age.toString)
      configureNamespace[Person]("person", None) shouldBe sample
    }

    "read lists and options" in {
      case class Book(pages: List[Int], chapters: List[String], author: Option[String])
      implicit val bookFormat: CF[Book] = format3(Book)
      val sample = Book(1 :: 2 :: 3 :: Nil, "One" :: "Two" :: Nil, Some("WispY"))
      configureNamespace[Book]("book", Some(sample)) shouldBe sample

      sys.props.put("book.pages", "[]")
      configureNamespace[Book]("book", Some(sample)) shouldBe sample.copy(pages = Nil)

      sys.props.remove("book.pages")
      sys.props.put("book.pages.0", "1")
      sys.props.put("book.pages.1", "2")
      sys.props.put("book.pages.2", "3")
      sys.props.put("book.chapters.0", "One")
      sys.props.put("book.chapters.1", "Two")
      sys.props.put("book.author.0", "WispY")
      configureNamespace[Book]("book", None) shouldBe sample
    }
  }

}