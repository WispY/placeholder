package cross.pac

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import cross.general.protocol.{LoginDiscord, User}
import cross.pac.protocol.{ArtChallenge, ChatMessage}
import spray.json.DefaultJsonProtocol._
import spray.json._

object json extends SprayJsonSupport {

  /** Simply wraps message list into an object */
  case class MessageList(messages: List[ChatMessage])

  /** Simply wraps user into an object */
  case class OptionUser(user: Option[User])

  implicit val loginDiscordFormat: RootJsonFormat[LoginDiscord] = jsonFormat1(LoginDiscord)
  implicit val userFormat: RootJsonFormat[User] = jsonFormat3(User)
  implicit val optionUserFormat: RootJsonFormat[OptionUser] = jsonFormat1(OptionUser)
  implicit val artChallengeFormat: RootJsonFormat[ArtChallenge] = jsonFormat0(ArtChallenge)
  implicit val messageFormat: RootJsonFormat[ChatMessage] = jsonFormat4(ChatMessage)
  implicit val messageListFormat: RootJsonFormat[MessageList] = jsonFormat1(MessageList)

}