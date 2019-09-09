package cross

import cross.binary._
import cross.common._
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

  /** Messages related to downloadable resources */
  sealed trait ResourceMessage extends Message

  /** Describes tileset stored in a file */
  case class TilesetAreas(areas: List[Rec2d]) extends ResourceMessage

  /** Messages related to lobby interactions */
  sealed trait LobbyMessage extends Message

  /** Messages related to game interactions */
  sealed trait GameMessage extends Message

  implicit val PlayerFormat = stringFormat.map[Player](Player.apply, a => a.id)
  implicit val SessionFormat = stringFormat.map[Session](Session.apply, a => a.id)
  implicit val Vec2dFormat = format2(Vec2d.apply)
  implicit val Rec2dFormat = format2(Rec2d.apply)
  implicit val TilesetAreasFormat = format1(TilesetAreas)

  val registry = Registry[Message](
    format1(Connect),
    format2(Connected),
    TilesetAreasFormat
  )
}