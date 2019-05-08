package cross

import cross.binary._
import cross.pattern._
import cross.subbinary.formats._
import cross.subbinary.registry.Registry

class BinarySpec extends Spec {
  val recompile = 1

  /** Marker trait for all messages */
  trait Message

  case class Person(name: String, age: Int) extends Message

  "binary" can {
    implicit val personFormat: BF[Person] = binaryFormat2(Person)

    def check[A](a: A)(implicit format: BF[A]): Unit = {
      ByteList.empty
        .chain { bytes => format.append(a, bytes) }
        .chain { bytes => format.read(bytes) shouldBe(a, ByteList.empty) }
    }

    "format person" in {
      check(Person("John", 33))
    }

    "format two persons" in {
      val personA = Person("John", 42)
      val personB = Person("Amelie", 24)
      ByteList.empty
        .chain { bytes => personFormat.append(personA, bytes) }
        .chain { bytes => personFormat.append(personB, bytes) }
        .chain { bytes =>
          val (person, next) = personFormat.read(bytes)
          person shouldBe personA
          next
        }
        .chain { bytes =>
          val (person, next) = personFormat.read(bytes)
          person shouldBe personB
          next shouldBe ByteList.empty
        }
    }

    "format types" in {
      case class Types(string: String, int: Int, double: Double, boolean: Boolean)
      implicit val typesFormat: BF[Types] = binaryFormat4(Types)

      check(Types("lorem", 42, 12.3, boolean = true))
      check(Types("", 0, -12.3, boolean = false))
    }

    "format lists" in {
      case class ListTypes(strings: List[String], ints: List[Int], doubles: List[Double], booleans: List[Boolean])
      implicit val listFormat: BF[ListTypes] = binaryFormat4(ListTypes)

      check(ListTypes("lorem" :: "ipsum" :: Nil, 1 :: 2 :: 3 :: Nil, 1.2 :: 3.4 :: Nil, true :: false :: Nil))
    }

    "defined for person" in {
      personFormat.isDefinedFor(Person("John", 42)) shouldBe true
      personFormat.isDefinedFor("John") shouldBe false
    }
  }

  "registry" can {
    case class Address(city: String, zip: Int) extends Message

    val registry = Registry[Message](
      binaryFormat2(Person),
      binaryFormat2(Address)
    )

    def check[A <: Message](a: A): Unit = {
      registry.write(a).validate { bytes =>
        registry.read(bytes) shouldBe(a, ByteList.empty)
      }
    }

    "format person" in {
      check(Person("John", 42))
    }

    "format address" in {
      check(Address("Jersey City", 1234))
    }
  }
}