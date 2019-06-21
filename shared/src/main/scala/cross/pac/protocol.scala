package cross.pac

import cross.binary._
import cross.format._
import cross.general.protocol._

object protocol {

  /** Describes a message from the user */
  case class ChatMessage(id: String, text: String, author: User, createTimestamp: Long)

  implicit val messageFormat: BF[ChatMessage] = format4(ChatMessage)

}