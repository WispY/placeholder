package cross.pac

import akka.actor.{Actor, ActorLogging}
import cross.pac.config.PacConfig
import net.dv8tion.jda.core.entities.{Message, MessageHistory, TextChannel}
import net.dv8tion.jda.core.events.message.{MessageReceivedEvent, MessageUpdateEvent}
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
    private var history: List[Message] = Nil

    override def preStart(): Unit = {
      log.info("starting...")
      val client = new JDABuilder(config.bot.token).build()
      client.addEventListener(new EventListener {
        override def onEvent(event: Event): Unit = event match {
          case e: ReadyEvent => self ! e
          case e: MessageReceivedEvent if e.getGuild.getName.equalsIgnoreCase(config.bot.server) && e.getChannel.getName == config.bot.channel => self ! e
          case e: MessageUpdateEvent if e.getGuild.getName.equalsIgnoreCase(config.bot.server) && e.getChannel.getName == config.bot.channel => self ! e
          case _ => // ignore
        }
      })
      clientOpt = Some(client)
      log.info("awaiting client readiness")
      context.become(awaitReady(client))
      context.system.scheduler.schedule(config.bot.historyRefreshDelay, config.bot.historyRefreshDelay, self, LoadRecentHistory)
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
                context.become(readFullHistory(channel, channel.getHistory))
                self ! LoadFullHistory
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

    /** Waits until the full channel history is loaded */
    def readFullHistory(channel: TextChannel, channelHistory: MessageHistory): Receive = {
      case LoadFullHistory =>
        log.info(s"retrieving 100 past messages, current size [${history.size}]")
        channelHistory.retrievePast(100).queue { messages => self ! PrependMessages(messages.asScala.toList) }

      case PrependMessages(messages) =>
        if (messages.isEmpty) {
          log.info(s"history was fully retrieved with size [${history.size}]")
          context.become(awaitCommands(channel, channelHistory))
        } else {
          history = messages.reverse ++ history
          self ! LoadFullHistory
        }

      case message =>
        log.info(s"received message when not loaded [$message], rescheduling...")
        context.system.scheduler.scheduleOnce(1.second, self, message)(ec, sender)
    }

    /** Waits for new reports */
    def awaitCommands(channel: TextChannel, channelHistory: MessageHistory): Receive = {
      case ReadMessages(sinceOpt) =>
        val since = sinceOpt.getOrElse(-1L)
        val list = history.filter(message => message.getCreationTime.toInstant.toEpochMilli >= since)
        sender ! MessagesResponse(list)

      case e: MessageReceivedEvent =>
        sender ! LoadRecentHistory

      case e: MessageUpdateEvent =>
        sender ! LoadRecentHistory

      case LoadRecentHistory =>
        log.info(s"loading recent history, current size [${history.size}]")
        channelHistory.retrieveFuture(100).queue { messages => self ! AppendMessages(messages.asScala.toList) }

      case AppendMessages(messages) =>
        history = history ++ messages.reverse
        if (messages.isEmpty) {
          log.info(s"done loading recent history, current size [${history.size}]")
        } else {
          self ! LoadRecentHistory
        }

      case _ => // ignore
    }

    /** Ignores all incoming reports */
    def ignoreMessages(): Receive = {
      case message => log.warning(s"ignoring message [$message]")
    }
  }

  /** Requests to load full channel history */
  object LoadFullHistory

  /** Requests to add recent messages to history */
  object LoadRecentHistory

  /** Prepends messages to channel history */
  case class PrependMessages(messages: List[Message])

  /** Appends messages to channel history */
  case class AppendMessages(messages: List[Message])

  /** Requests to read all messages starting from given timestamp
    *
    * @param since Some(timestamp) to start reading from, None if whole channel should be scanned
    */
  case class ReadMessages(since: Option[Long])

  /** Contains messages read from some timestamp
    *
    * @param messages a list of messages from admin users
    */
  case class MessagesResponse(messages: List[Message])

}