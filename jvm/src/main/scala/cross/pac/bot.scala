package cross.pac

import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

import akka.actor.{Actor, ActorLogging}
import cross.pac.config.PacConfig
import net.dv8tion.jda.core.entities.{Message, TextChannel}
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

    override def preStart(): Unit = {
      log.info("starting...")
      val client = new JDABuilder(config.discordToken).build()
      client.addEventListener(new EventListener {
        override def onEvent(event: Event): Unit = self ! event
      })
      clientOpt = Some(client)
      log.info("awaiting client readiness")
      context.become(awaitReady(client))
      self ! PrintChat
    }

    override def receive: Receive = Actor.emptyBehavior

    /** Waits till discord client is ready */
    def awaitReady(client: JDA): Receive = {
      case event: ReadyEvent =>
        log.info(s"discord bot is ready [$event]")
        client
          .getTextChannels
          .asScala
          .find(channel => channel.getName == config.discordChannel) match {
          case Some(channel) =>
            log.info(s"found channel [${config.discordChannel}]")
            context.become(awaitMessages(channel))
          case None =>
            log.warning(s"failed to find channel [${config.discordChannel}]")
            context.become(ignoreMessages())
        }

      case message =>
        log.info("received message when not ready, rescheduling...")
        context.system.scheduler.scheduleOnce(1.second, self, message)
    }

    /** Waits for new reports */
    def awaitMessages(channel: TextChannel): Receive = {
      case PrintChat =>
        log.info(s"retrieving 50 past messages")
        channel.getHistory.retrievePast(50).queue { messages => self ! PrintMessages(messages.asScala.toList) }

      case PrintMessages(messages) =>
        log.info(s"printing all chat messages")
        messages.foreach { m =>
          val timestamp = ISO_OFFSET_DATE_TIME.format(m.getCreationTime)
          val author = m.getAuthor.getName
          val content = m.getContentRaw
          val attachments = m.getAttachments.asScala.map(a => a.getUrl).mkString(", ")
          val reactions = m.getReactions.asScala.map(r => r.getReactionEmote.getEmote).mkString(", ")
          log.info(s"[$timestamp] $author: $content --- $attachments --- $reactions")
        }

      case _ => // ignore
    }

    /** Ignores all incoming reports */
    def ignoreMessages(): Receive = {
      case message => log.warning(s"ignoring message [$message]")
    }
  }

  /** Requests to print all messages from chat */
  object PrintChat

  /** Requests to print given messages */
  case class PrintMessages(messages: List[Message])

}