package cross

class WeirdScalaSpec extends Spec {
  "weird scala" can {
    "die compiling math" in {
      val xOpt: Option[Double] = Some(1.0)
      val x = xOpt.getOrElse(0)
      val y: Double = 2.0
      val result: Double = x + y * 3
      println(result)
    }
  }
}