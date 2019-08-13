package cross.pac

import cross.general.protocol._

object protocol {

  /** Describes an art challenge topic held within some time span */
  case class ArtChallenge()

  /** Describes a message from the user */
  case class ChatMessage(id: String, text: String, author: User, createTimestamp: Long)

}