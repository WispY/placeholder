package cross

//import akka.util.ByteString
import cross.binary._

class BinarySpec extends Spec {
  val recompile = 2

  case class Person(name: String, age: Int)

  "binary" can {
    "format person" in {
      implicit val personFormat: BF[Person] = binaryFormat2(Person)
      val person = Person("hello", 42)
//      ByteString.empty
//      val bytes = personFormat.append(person, ByteString.empty)
//      personFormat.read(bytes) shouldBe(person, ByteString.empty)
    }
  }
}