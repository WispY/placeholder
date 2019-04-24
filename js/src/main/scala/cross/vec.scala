package cross

object vec {

  case class Vec2i(x: Int, y: Int) {
    def +(v: Vec2i): Vec2i = Vec2i(x + v.x, y + v.y)

    def +(v: Vec2d): Vec2d = Vec2d(x + v.x, y + v.y)

    def *(m: Int): Vec2i = Vec2i(x * m, y * m)

    def *(d: Double): Vec2d = Vec2d(x * d, y * d)

    def *(v: Vec2d): Vec2d = Vec2d(x * v.x, y * v.y)

    def /(d: Double): Vec2d = Vec2d(x / d, y / d)

    def /(v: Vec2i): Vec2d = Vec2d(x / v.x.toDouble, y / v.y.toDouble)

    def style: String = s"width: ${x}px; height: ${y}px; "

    def range: List[Int] = (x to y).toList

    def start: Int = x

    def end: Int = x

    def flipY: Vec2i = Vec2i(x, -y)

    def flipX: Vec2i = Vec2i(-x, y)

    def flip: Vec2i = Vec2i(-x, -y)

    def mapY(code: Int => Int) = Vec2i(x, code.apply(y))
  }

  object Vec2i {
    val Zero: Vec2i = 0 xy 0
  }

  case class Vec2d(x: Double, y: Double) {
    def +(v: Vec2d): Vec2d = Vec2d(x + v.x, y + v.y)

    def *(m: Double): Vec2d = Vec2d(x * m, y * m)

    def *(v: Vec2d): Vec2d = Vec2d(x * v.x, y * v.y)

    def -(v: Vec2d): Vec2d = Vec2d(x - v.x, y - v.y)

    def min: Double = x min y

    def max: Double = x max y
  }

  object Vec2d {
    val Center: Vec2d = 0.5 xy 0.5

    val Top: Vec2d = 0.5 xy 0.0

    val Bottom: Vec2d = 0.5 xy 1.0

    val Zero: Vec2d = 0.0 xy 0.0
  }

  implicit class VecIntOps(val int: Int) extends AnyVal {
    /** Creates a vector from two numbers */
    def xy(y: Int): Vec2i = Vec2i(int, y)

    /** Creates a vector from two numbers */
    def xy(y: Double): Vec2d = Vec2d(int, y)
  }

  implicit class VecDoubleOps(val double: Double) extends AnyVal {
    /** Creates a vector from two numbers */
    def xy(y: Double): Vec2d = Vec2d(double, y)
  }

  implicit class VecDoubleTupleOps(val tuple: (Vec2d, Vec2d)) extends AnyVal {
    /** Returns a vec between two components according to progress */
    def %%(progress: Double): Vec2d = tuple._1 + (tuple._2 - tuple._1) * progress
  }

}