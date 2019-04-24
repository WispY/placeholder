package cross

import scala.util.Random

object random {

  implicit class RandomOps(val random: Random) extends AnyVal {
    /** Returns a random element from the sequence */
    def oneOf[A](list: List[A]): A = list(random.nextInt(list.size))

    /** Returns a subset of random element from the given seq */
    def multipleOf[A](list: List[A], size: Int): List[A] = size match {
      case tooMuch if size > list.size => throw new IllegalArgumentException(s"random seq size must not be greater than original seq size: expected <= ${list.size}, got $size")
      case sameSize if size == list.size => random.shuffle(list)
      case other => random.shuffle(list).take(other)
    }

    /** Returns a number between start and end inclusive */
    def between(start: Int, end: Int): Int = {
      random.nextInt(end - start + 1) + start
    }
  }

}