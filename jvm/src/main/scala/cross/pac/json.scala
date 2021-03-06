package cross.pac

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import cross.general.protocol.{LoginDiscord, User}
import cross.pac.protocol._
import cross.pac.routes.SystemStatus
import spray.json.DefaultJsonProtocol._
import spray.json._

object json extends SprayJsonSupport {

  /** Represents an empty json body */
  case class EmptyJson(foo: Option[String] = None)

  /** Simply wraps user into an object */
  case class OptionUser(user: Option[User])

  /** Simply wraps status list into an object */
  case class StatusList(statuses: List[SystemStatus])

  implicit val emptyJsonFormat: RootJsonFormat[EmptyJson] = jsonFormat1(EmptyJson)
  implicit val loginDiscordFormat: RootJsonFormat[LoginDiscord] = jsonFormat2(LoginDiscord)
  implicit val userFormat: RootJsonFormat[User] = jsonFormat4(User)
  implicit val optionUserFormat: RootJsonFormat[OptionUser] = jsonFormat1(OptionUser)
  implicit val artChallengeFormat: RootJsonFormat[ArtChallenge] = jsonFormat6(ArtChallenge)
  implicit val artChallengeListFormat: RootJsonFormat[ArtChallengeList] = jsonFormat4(ArtChallengeList)
  implicit val messageFormat: RootJsonFormat[ChatMessage] = jsonFormat4(ChatMessage)
  implicit val messageListFormat: RootJsonFormat[MessageList] = jsonFormat4(MessageList)
  implicit val systemStatusFormat: RootJsonFormat[SystemStatus] = jsonFormat3(SystemStatus)
  implicit val statusListFormat: RootJsonFormat[StatusList] = jsonFormat1(StatusList)
  implicit val submissionResourceFormat: RootJsonFormat[SubmissionResource] = jsonFormat3(SubmissionResource)
  implicit val submissionFormat: RootJsonFormat[Submission] = jsonFormat5(Submission)
  implicit val submissionListFormat: RootJsonFormat[SubmissionList] = jsonFormat4(SubmissionList)

}