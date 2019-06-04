package cross

import java.util.UUID

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

object common {

  /** Returns random uuid */
  def uuid: String = UUID.randomUUID().toString

  implicit class AnyOps[A](val a: A) extends AnyVal {
    /** Chains the execution to the given code block */
    def chain[B](code: A => B): B = code.apply(a)

    /** Executes the given code and returns the initial unmodified value */
    def validate(code: A => Unit): A = {
      code.apply(a)
      a
    }

    /** Chains the call for some value */
    def chainOpt[B](opt: Option[B])(code: (A, B) => A): A = opt match {
      case Some(b) => code.apply(a, b)
      case None => a
    }

    /** Calls the mutating code */
    def mutate(code: A => Unit): A = {
      code.apply(a)
      a
    }

    /** Calls the mutating code for some value */
    def mutateOpt[B](opt: Option[B])(code: (A, B) => Unit): A = opt match {
      case Some(b) => code.apply(a, b); a
      case None => a
    }

    /** Transforms the value with given code */
    def transform[B](code: A => B): B = code.apply(a)

    /** Wraps the value into the future */
    def future: Future[A] = Future.successful(a)
  }

  implicit class DoubleOps(val double: Double) extends AnyVal {
    /** Formats the given double with given number of digits after point */
    def pretty(digits: Int = 2): String = String.format(s"%.${digits}f", double: java.lang.Double)

    /** Normalizes the rotation by removing extra PIs */
    def normalRotation: Double = {
      if (double > Math.PI || double < -Math.PI) double - Math.PI * (double / Math.PI).toInt
      else double
    }

    /** Converts value to radians */
    def rad: Double = double / 180 * Math.PI
  }

  implicit class DoubleTupleOps(val tuple: (Double, Double)) extends AnyVal {
    /** Returns a number between two components according to progress */
    def %%(progress: Double): Double = tuple._1 + (tuple._2 - tuple._1) * progress

    /** Returns a number either at the end of the range or between the range */
    def %%%(range: (Double, Double), current: Double): Double = {
      if (current <= range._1) {
        tuple._1
      } else if (current >= range._2) {
        tuple._2
      } else {
        tuple %% ((current - range._1) / (range._2 - range._1))
      }
    }

    /** Returns a rotation between two components according to progress, normalizes the components */
    def rotationProgress(progress: Double): Double = {
      val (a, b) = tuple
      val (an, bn) = (a.normalRotation, b.normalRotation)
      val diff = List(bn - an, bn + Math.PI * 2 - an, bn - Math.PI * 2 - an).minBy(diff => diff.abs)
      (a, a + diff) %% progress
    }
  }

  implicit class TraversableOps[A](val list: Traversable[A]) extends AnyVal {
    /** Safely calculates min for non-empty seqs */
    def minOpt(implicit ordering: Ordering[A]): Option[A] = list match {
      case empty if empty.isEmpty => None
      case values => Some(values.min)
    }

    /** Safely calculates max for non-empty seqs */
    def maxOpt(implicit ordering: Ordering[A]): Option[A] = list match {
      case empty if empty.isEmpty => None
      case values => Some(values.max)
    }
  }

  implicit class MapOps[A, B](val map: Map[A, B]) extends AnyVal {
    /** Ensures that value exists in mutable map reference */
    def ensure(key: A, rewrite: Map[A, B] => Unit)(code: => B): B = {
      map.get(key) match {
        case Some(value) => value
        case None =>
          val value = code
          rewrite.apply(map + (key -> value))
          value
      }
    }
  }

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

  implicit class ListVec2iOps(val list: Traversable[Vec2i]) extends AnyVal {
    /** Returns the min to max range of X values */
    def rangeX: Option[Vec2i] = list match {
      case empty if empty.isEmpty => None
      case nonempty =>
        val xs = list.map(v => v.x)
        Some(xs.min xy xs.max)
    }

    /** Returns the min to max range of Y values */
    def rangeY: Option[Vec2i] = list match {
      case empty if empty.isEmpty => None
      case nonempty =>
        val ys = list.map(v => v.y)
        Some(ys.min xy ys.max)
    }
  }

  implicit class FiniteDurationOps(val duration: FiniteDuration) extends AnyVal {
    /** Multiplies the duration by given number */
    def **(multiplier: Double): FiniteDuration = (duration.toMillis * multiplier).toLong.millis
  }

  type TransitionListener[A] = PartialFunction[(A, A), Unit]
  type EndListener[A] = PartialFunction[A, Unit]

  /** Wraps the mutable value for subscriptions */
  trait Data[A] {
    /** Returns the latest value */
    def read: A

    /** Adds the given listener that will be invoked on every update */
    def listen(code: TransitionListener[A], initialize: Boolean): Data[A]

    /** Partially projects the wrapped value with given code */
    def partialMap[B](code: PartialFunction[A, B]): Data[Option[B]]

    /** Maps the wrapped value with given code */
    def map[B](code: A => B): Data[B]

    /** Combines the wrapper with another one for simultaneous subscriptions */
    def and[B](other: Data[B]): Data[(A, B)]


    /** Returns the latest value */
    def apply(): A = this.read

    /** Adds the given listener that will be invoked on every update */
    def />>(code: TransitionListener[A]): Data[A] = {
      this.listen(code, initialize = true)
    }

    /** Adds the given listener that will be invoked on every update */
    def />(code: EndListener[A]): Data[A] = {
      val listener: TransitionListener[A] = {
        case (before, after) if code.isDefinedAt(after) => code.apply(after)
      }
      this.listen(listener, initialize = true)
    }

    /** Partially projects the wrapped value with given code */
    def /~[B](code: PartialFunction[A, B]): Data[Option[B]] = this.partialMap(code)

    /** Combines the wrapper with another one for simultaneous subscriptions */
    def &&[B](other: Data[B]): Data[(A, B)] = this.and(other)
  }

  /** Represents the wrapper that can mutate it's value */
  trait Writeable[A] extends Data[A] {
    /** Mutates the current value into a given one */
    def write(a: A): A
  }

  private class Implementation[A](default: A) extends Writeable[A] {
    private var value: A = default
    private var listeners: List[TransitionListener[A]] = Nil

    override def partialMap[B](code: PartialFunction[A, B]): Data[Option[B]] = {
      new Implementation[Option[B]](code.lift.apply(value)).mutate { source =>
        this.listen({ case (before, after) =>
          val current = source.read
          val next = code.lift.apply(after)
          if (current != next) source.write(next)
        }, initialize = true)
      }
    }

    override def read: A = value

    override def map[B](code: A => B): Data[B] = {
      new Implementation(code.apply(value)).mutate { source =>
        this.listen({ case (before, after) => source.write(code.apply(after)) }, initialize = true)
      }
    }

    override def write(a: A): A = this.synchronized {
      val before = value
      val after = a
      value = after
      listeners.foreach { listener => listener.lift.apply(before, after) }
      value
    }

    override def listen(code: TransitionListener[A], initialize: Boolean): Data[A] = this.synchronized {
      listeners = listeners :+ code
      if (initialize) {
        code.lift.apply(value, value)
      }
      this
    }

    override def and[B](other: Data[B]): Data[(A, B)] = {
      new Implementation((this.read, other.read))
        .mutate { source =>
          val handler: TransitionListener[Any] = source.synchronized {
            case _ => source.write(this.read, other.read)
          }

          this.listen(handler, initialize = true)
          other.listen(handler, initialize = true)
        }
    }
  }

  object Data {
    /** Creates the writable data source */
    def source[A](default: A): Writeable[A] = new Implementation(default)

    /** Creates the writable data source */
    def apply[A](default: A): Writeable[A] = Data.source(default)
  }

  implicit class DataOptionOps[A](val data: Writeable[Option[A]]) extends AnyVal {
    /** Sets optional value to Some given value */
    def writeSome(a: A): Unit = data.write(Some(a))

    /** Updates the optional value, if present */
    def update(code: A => A): Unit = {
      val current = data.read
      val next = current.map(code)
      next.foreach(v => data.write(Some(v)))
    }
  }

  implicit class DataOptionOptionOps[A](val data: Data[Option[Option[A]]]) extends AnyVal {
    /** Unwraps the inner option for double option data */
    def flatten: Data[Option[A]] = data /~ { case Some(Some(a)) => a }
  }

  implicit class FutureOps[A](val future: Future[A]) extends AnyVal {
    /** Clears the future value into unit */
    def clear(implicit ec: ExecutionContext): Future[Unit] = future.map(a => ())

    /** Appends the next future without data dependency */
    def >>[B](other: Future[B])(implicit ec: ExecutionContext): Future[Unit] = future.flatMap(any => other).clear
  }

  implicit class AnyListOps[A](val list: List[A]) extends AnyVal {
    /** Creates a processing chain from the list */
    def chainProcessing[B](last: Any, constructor: (A, Any) => B): Any = {
      list.foldRight(last) { case (element, next) => constructor.apply(element, next) }
    }
  }

  /** Integer 2D vector */
  case class Vec2i(x: Int, y: Int) {
    /** Adds components of another vector */
    def +(v: Vec2i): Vec2i = Vec2i(x + v.x, y + v.y)

    /** Adds components of another vector */
    def +(v: Vec2d): Vec2d = Vec2d(x + v.x, y + v.y)

    /** Multiplies components by given number */
    def *(m: Int): Vec2i = Vec2i(x * m, y * m)

    /** Multiplies components by given number */
    def *(d: Double): Vec2d = Vec2d(x * d, y * d)

    /** Multiplies components by given vector components */
    def *(v: Vec2d): Vec2d = Vec2d(x * v.x, y * v.y)

    /** Divides components by given vector components */
    def /(d: Double): Vec2d = Vec2d(x / d, y / d)

    /** Divides components by given vector components */
    def /(v: Vec2i): Vec2d = Vec2d(x / v.x.toDouble, y / v.y.toDouble)

    /** Converts vector into css style string */
    def style: String = s"width: ${x}px; height: ${y}px; "

    /** Converts vector into a list of indices */
    def range: List[Int] = (x to y).toList

    /** Negates the components */
    def flip: Vec2i = Vec2i(-x, -y)

    /** Negates the X */
    def flipX: Vec2i = Vec2i(-x, y)

    /** Negates the Y */
    def flipY: Vec2i = Vec2i(x, -y)

    /** Maps the X */
    def mapX(code: Int => Int) = Vec2i(code.apply(x), y)

    /** Maps the Y */
    def mapY(code: Int => Int) = Vec2i(x, code.apply(y))
  }

  object Vec2i {
    /** Origin point */
    val Zero: Vec2i = 0 xy 0
  }

  /** Double 2D vector */
  case class Vec2d(x: Double, y: Double) {
    /** Adds components of another vector */
    def +(v: Vec2d): Vec2d = Vec2d(x + v.x, y + v.y)

    /** Subtracts components of another vector */
    def -(v: Vec2d): Vec2d = Vec2d(x - v.x, y - v.y)

    /** Multiplies components by given number */
    def *(m: Double): Vec2d = Vec2d(x * m, y * m)

    /** Multiplies components by given vector components */
    def *(v: Vec2d): Vec2d = Vec2d(x * v.x, y * v.y)

    /** Returns minimum component */
    def min: Double = x min y

    /** Returns maximum component */
    def max: Double = x max y

    /** Returns true if given point is within the radius of the vector */
    def near(other: Vec2d, radius: Double): Boolean = {
      val diff = this - other
      val squared = diff * diff
      squared.x + squared.y < radius * radius
    }
  }

  object Vec2d {
    /** Center point in 1x1 square */
    val Center: Vec2d = 0.5 xy 0.5

    /** Top point in 1x1 square */
    val Top: Vec2d = 0.5 xy 0.0

    /** Bottom point in 1x1 square */
    val Bottom: Vec2d = 0.5 xy 1.0

    /** Origin point */
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