package cross.pac.stage

import cross.common.{Data, Writeable}
import cross.layout._
import cross.ops._
import cross.pac.config.ManageConfig
import cross.pac.mvc._
import cross.pac.protocol.ChatMessage
import cross.util.logging.Logging

class ManagePage()(implicit config: ManageConfig, controller: Controller) extends Logging {
  override protected def logKey: String = "pac/manage"

  private lazy val layout = xbox.space(config.space).fillBoth.children(
    ybox.fillBoth.space(config.space).children(
      messagesBox,
      filler,
      messagesPaginator
    ),
    scroll(config.scroll).alignTop.width(config.challengesWidth).fillY
  )
  private lazy val messagesLoading = label("", config.messagesLoadingLabelStyle)
  private lazy val messagesBox = box.fillBoth.children(messagesLoading, messagesList)
  private lazy val messagesList = ybox.space(config.messagesSpace)
  private lazy val messagesPaginator = paginator(config.messagesPaginatorStyle, controller.model.adminMessages.view(Nil), messagesData)
  private lazy val messagesData: Writeable[List[ChatMessage]] = Data(Nil)

  private lazy val challenges = null
  this.init()

  /** Returns the entire layout of the page */
  def pageLayout: LayoutBox = layout

  /** Initializes the page */
  private def init(): Unit = {
    controller.model.adminMessages
      .onLoading {
        messagesLoading.label("Loading messages...").visible(true)
        messagesList.visible(false)
      }
      .onLoad { messages =>
        messagesLoading.visible(false)
        messagesList.visible(true)
      }
      .onFailed { error =>
        messagesLoading.label(error).visible(true)
        messagesList.visible(false)
      }

    messagesData /> { case messages =>
      val buttons = messages.map { message =>
        val line = message.text.replaceAll("\n", " ")
        val text = s"${message.author.name}: $line"
        button(config.messagesButtonStyle).fillX.pad(config.messagesPad).children(
          fillLabel(text, config.messagesMaxLength, config.messagesLabelStyle)
        )
      }
      messagesList.children(buttons: _*)
    }
  }

}