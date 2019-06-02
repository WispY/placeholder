package cross

import cross.binary._
import cross.format._

/** Contains all messages to interact with server via websockets */
//noinspection TypeAnnotation
object protocol {

  /** All client-server messages */
  sealed trait Message

  /** Messages related to game and lobby manager/coordinator */
  sealed trait ManagerMessage extends Message

  /** The player identifier */
  case class Player(id: String)

  /** The connection session identifier */
  case class Session(id: String)

  /** Requests to ping the server */
  case class Ping() extends ManagerMessage

  /** Requests to connect to server
    *
    * @param session the optional session id from previous interactions
    */
  case class Connect(session: Option[Session]) extends ManagerMessage

  /** Indicates that the player has successfully connected to the server
    *
    * @param session the id of the connection (private)
    * @param player  the id of the connected player (public)
    */
  case class Connected(session: Session, player: Player) extends ManagerMessage

  /** Messages related to lobby interactions */
  sealed trait LobbyMessage extends Message

  /** Messages related to game interactions */
  sealed trait GameMessage extends Message

  implicit val f0 = stringFormat.map[Player](Player.apply, a => a.id)
  implicit val f1 = stringFormat.map[Session](Session.apply, a => a.id)

  val registry = Registry[Message](
    format1(Connect),
    format2(Connected)
  )
}