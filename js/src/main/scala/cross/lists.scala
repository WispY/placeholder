package cross

object lists {

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

}