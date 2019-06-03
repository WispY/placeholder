package cross

import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

trait Spec extends WordSpecLike with Matchers with ScalaFutures with IntegrationPatience with Eventually with BeforeAndAfter with BeforeAndAfterAll