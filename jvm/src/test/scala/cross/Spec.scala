package cross

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfter, Matchers, WordSpecLike}

trait Spec extends WordSpecLike with Matchers with ScalaFutures with IntegrationPatience with BeforeAndAfter