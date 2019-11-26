package cross.general

object protocol {

  /** Logs in the discord user
    *
    * @param code     the code value contained in discord redirection url as a query parameter
    * @param redirect Some(custom url) where discord redirects after login, None if server default can be used
    */
  case class LoginDiscord(code: String, redirect: Option[String])

  /** Contains discord user information
    *
    * @param id     the discord id of the user
    * @param name   the displayed discord name of the user
    * @param avatar the url to user's uploaded avatar or default avatar
    * @param admin  true, if user is recognized as project admin
    */
  case class User(id: String, name: String, avatar: String, admin: Boolean)

}