package cross.general

import cross.binary._
import cross.format._

object protocol {

  /** Logs in the discord user */
  case class LoginDiscord(code: String)

  /** Contains discord user information */
  case class User(id: String, name: String, admin: Boolean)

  implicit val loginDiscordFormat: BF[LoginDiscord] = format1(LoginDiscord)
  implicit val userFormat: BF[User] = format3(User)

}