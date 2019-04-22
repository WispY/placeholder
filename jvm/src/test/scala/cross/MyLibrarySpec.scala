package cross

class MyLibrarySpec extends Spec {
  "MyLibrary" can {
    "return square of 2" in {
      new MyLibrary().sq(2) shouldBe 4
    }
  }
}