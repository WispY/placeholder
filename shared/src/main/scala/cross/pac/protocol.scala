package cross.pac

import cross.binary._
import cross.format._
import cross.general.protocol._

object protocol {

  /** Describes a message from the user */
  case class Message(id: String, text: String, author: User, createTimestamp: Long)

  implicit val messageFormat: BF[Message] = format4(Message)

}