package cross

object pattern {

  implicit class PatternAnyOps[A](val any: A) extends AnyVal {
    /** Chains the execution to the given code block */
    def chain[B](code: A => B): B = code.apply(any)

    /** Executes the given code and returns the initial unmodified value */
    def validate(code: A => Unit): A = {
      code.apply(any)
      any
    }
  }

}