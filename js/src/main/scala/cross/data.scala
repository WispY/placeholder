package cross

import cross.global.GlobalContext
import cross.logging.Logging
import cross.pattern._

object data extends GlobalContext {

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

  private class Implementation[A](default: A) extends Writeable[A] with Logging {
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

}