package cross.general

import cross.binary._
import cross.format._

object protocol {

  /** Logs in the discord user
    *
    * @param code the code value contained in discord redirection url as a query parameter
    */
  case class LoginDiscord(code: String)

  /** Contains discord user information
    *
    * @param id    the discord id of the user
    * @param name  the displayed discord name of the user
    * @param admin true, if user is recognized as project admin
    */
  case class User(id: String, name: String, admin: Boolean)

  implicit val loginDiscordFormat: BF[LoginDiscord] = format1(LoginDiscord)
  implicit val userFormat: BF[User] = format3(User)

}