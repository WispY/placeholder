package cross

import scala.concurrent.Future

object pattern {

  implicit class PatternAnyOps[A](val a: A) extends AnyVal {
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

}