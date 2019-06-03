package cross

import java.util.UUID

import com.github.simplyscala.{MongoEmbedDatabase, MongodProps}
import com.typesafe.scalalogging.LazyLogging
import cross.format._
import cross.mongo._
import org.mongodb.scala.{MongoClient, Observable, SingleObservable}

class MongoSpec extends Spec with MongoEmbedDatabase with LazyLogging {
  private var process: MongodProps = _
  private var client: MongoClient = _

  case class Person(name: String, age: Int)

  "mongo" can {
    "write and read person" in {
      implicit val personFormat: MF[Person] = format2(Person)
      check(Person("John", 43))
    }

    "write and read lists and options" in {
      case class Collections(strings: List[String], people: List[Person], intOpt: Option[Int], personOpt: Option[Person])
      implicit val personFormat: MF[Person] = format2(Person)
      implicit val collectionsFormat: MF[Collections] = format4(Collections)
      check(Collections(
        strings = "foo" :: "bar" :: "baz" :: Nil,
        people = Person("John", 43) :: Person("Jeremy", 34) :: Nil,
        intOpt = Some(42),
        personOpt = Some(Person("Mary", 75))
      ))
      check(Collections(
        strings = Nil,
        people = Nil,
        intOpt = None,
        personOpt = None
      ))
    }
  }

  def check[A <: AnyRef](a: A)(implicit format: MF[A]): Unit = {
    val collectionName = UUID.randomUUID().toString
    val db = client.getDatabase("test")
    db.createCollection(collectionName).await
    val collection = db.getCollection(collectionName)
    collection.insertOne(a.asBson).await
    collection.find().await.head.asScala[A] shouldBe a
  }

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    logger.info("starting test mongo instance")
    process = eventually(mongoStart())
    client = MongoClient("mongodb://127.0.0.1:12345/")
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    logger.info("stopping test mongo instance")
    Option(process).foreach(p => mongoStop(p))
    Option(client).foreach(c => c.close())
    logger.info("stopped test mongo instance")
  }

  implicit class SingleObservableOps[A](obs: SingleObservable[A]) {
    def await: A = obs.toFuture().futureValue
  }

  implicit class ObservableOps[A](obs: Observable[A]) {
    def await: List[A] = obs.toFuture().futureValue.toList
  }

}