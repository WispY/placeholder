package cross.pac

import akka.actor.{Actor, ActorLogging, ActorRef}
import cross.common._
import cross.pac.config.PacConfig
import net.dv8tion.jda.core.entities.{Message, MessageHistory, TextChannel}
import net.dv8tion.jda.core.events.message.{GenericMessageEvent, MessageDeleteEvent, MessageReceivedEvent, MessageUpdateEvent}
import net.dv8tion.jda.core.events.{Event, ReadyEvent}
import net.dv8tion.jda.core.hooks.EventListener
import net.dv8tion.jda.core.{JDA, JDABuilder}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object bot {

  /** Interacts with Discord as art challenge bot */
  class ArtChallengeBot(config: PacConfig) extends Actor with ActorLogging {
    private implicit val ec: ExecutionContext = context.dispatcher
    private var clientOpt: Option[JDA] = None
    private var listeners: List[ActorRef] = Nil

    override def preStart(): Unit = {
      log.info("starting...")
      val client = new JDABuilder(config.bot.token).build()
      client.addEventListener(new EventListener {
        override def onEvent(event: Event): Unit = event match {
          case e: ReadyEvent => self ! e
          case e: MessageReceivedEvent if isRelevant(e) => self ! e
          case e: MessageUpdateEvent if isRelevant(e) => self ! e
          case e: MessageDeleteEvent if isRelevant(e) => self ! e
          case _ => // ignore
        }
      })
      clientOpt = Some(client)
      log.info("awaiting client readiness")
      context.become(awaitReady(client))
    }

    /** Returns true if event happened on configured server and channel */
    private def isRelevant(event: GenericMessageEvent): Boolean = {
      event.getGuild.getName.equalsIgnoreCase(config.bot.server) && event.getChannel.getName == config.bot.channel
    }

    override def postStop(): Unit = {
      clientOpt.foreach(client => client.shutdown())
    }

    override def receive: Receive = Actor.emptyBehavior

    /** Waits till discord client is ready */
    def awaitReady(client: JDA): Receive = {
      case event: ReadyEvent =>
        log.info(s"discord bot is ready [$event] with servers [${client.getGuilds.asScala.map(g => g.getName).toList}]")
        client.getGuildsByName(config.bot.server, true).asScala.headOption match {
          case Some(server) =>
            log.info(s"found server [${config.bot.server}]")
            server
              .getTextChannels.asScala
              .find(channel => channel.getName == config.bot.channel) match {
              case Some(channel) =>
                log.info(s"found channel [${config.bot.channel}]")
                context.become(awaitCommands(channel))
              case None =>
                log.warning(s"failed to find channel [${config.bot.channel}]")
                context.become(ignoreMessages())
            }
          case None =>
            log.warning(s"failed to find server [${config.bot.server}]")
            context.become(ignoreMessages())
        }

      case message =>
        log.info(s"received message when not ready [$message], rescheduling...")
        context.system.scheduler.scheduleOnce(1.second, self, message)(ec, sender)
    }

    /** Waits for new requests */
    def awaitCommands(channel: TextChannel): Receive = {
      case event: MessageReceivedEvent =>
        listeners.foreach(listener => listener ! MessagePosted(event.getMessage))

      case event: MessageUpdateEvent =>
        listeners.foreach(listener => listener ! MessageEdited(event.getMessage))

      case event: MessageDeleteEvent =>
        listeners.foreach(listener => listener ! MessageDeleted(event.getMessageId))

      case request: UpdateHistory =>
        val history = channel.getHistory
        val listener = sender
        listeners = listeners :+ listener
        history.retrievePast(100).queue { messages =>
          log.info(s"retrieved initial [${messages.size()}] messages for history update")
          self ! UpdateHistoryChain(listener, messages.asScala.toList, history, request)
        }

      case UpdateHistoryChain(listener, chunk, history, request) =>
        if (chunk.nonEmpty) {
          chunk.foreach {
            case irrelevant if irrelevant.getCreationTime < request.since.getOrElse(-1) =>
              log.info(s"found message in history outside of requested time [${irrelevant.getId}]")
            case upToDate if request.expected.get(upToDate.getId).contains(Option(upToDate.getEditedTime).getOrElse(upToDate.getCreationTime).epoch) =>
              log.info(s"found up to date message in history [${upToDate.getId}]")
            case outdated if request.expected.contains(outdated.getId) =>
              log.info(s"found outdated message in history [${outdated.getId}]")
              listener ! MessageEdited(outdated)
            case unknown =>
              log.info(s"found new message in history [${unknown.getId}]")
              listener ! MessagePosted(unknown)
          }
          val diff = request.expected.filterNot { case (id, timestamp) => chunk.exists(message => message.getId == id) }
          if (chunk.last.getCreationTime < request.since.getOrElse(-1)) {
            log.info("reached end of requested history")
            diff.keys.foreach(id => listener ! MessageDeleted(id))
          } else {
            history.retrievePast(100).queue { messages =>
              log.info(s"retrieved next [${messages.size()}] messages for history update")
              self ! UpdateHistoryChain(listener, messages.asScala.toList, history, request.copy(expected = diff))
            }
          }
        } else {
          log.info("reached end of history")
        }

      case _ => // ignore
    }

    /** Ignores all incoming reports */
    def ignoreMessages(): Receive = {
      case message => log.warning(s"ignoring message [$message]")
    }
  }

  /** Updates the message history from given time comparing it to given values
    *
    * @param since    Some(timestamp) from which history should be read, None for full history
    * @param expected message id to last update timestamp that are stored in the system
    */
  case class UpdateHistory(since: Option[Long], expected: Map[String, Long])

  /** Chains the history update calls */
  private case class UpdateHistoryChain(listener: ActorRef, chunk: List[Message], history: MessageHistory, request: UpdateHistory)

  /** Indicates that new message was posted */
  case class MessagePosted(message: Message)

  /** Indicates that existing message was edited in the channel */
  case class MessageEdited(message: Message)

  /** Indicates that message was removed from the channel */
  case class MessageDeleted(id: String)

}