package cross.robot

import java.awt.event.InputEvent
import java.awt.image.BufferedImage
import java.awt.{Desktop, MouseInfo, Rectangle, Robot}
import java.io.File
import java.nio.file.Files
import java.util.logging.{Level, LogManager, Logger}

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.scalalogging.StrictLogging
import cross.common._
import javax.imageio.ImageIO
import org.jnativehook._
import org.jnativehook.keyboard.{NativeKeyEvent, NativeKeyListener}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

//noinspection TypeAnnotation
object PoeChromatic extends App with StrictLogging {
  val desktop = Desktop.getDesktop
  val robot = new Robot()
  val templates = new File("C:/Users/WispY/Desktop/bot")

  val keyboard: Writeable[Int] = Data(-1)
  val enabled: Writeable[Boolean] = Data(false)
  val tick: Writeable[Long] = Data(0)
  val screenshot: Writeable[Option[BufferedImage]] = Data(None)
  val screenshotRect: Writeable[Rec2i] = LazyData(Rec2i.Zero)
  initKeyboardListener()

  tick /> {
    case 0 => // ignore
    case _ =>
      val mouse = mouseLocation
      logger.info(s"mouse [${mouse.x} xy ${mouse.y}]")
      if (isMinimized) {
        logger.info("game is minimized, maximizing...")
        mouseClick(225 xy 1413)
      }
  }

  keyboard /> {
    /** F9 - enable/disable bot */
    case 67 =>
      enabled.write(!enabled())

    /** F2 - start/end screenshot */
    case 60 =>
      if (enabled()) {
        screenshot() match {
          case None =>
            logger.info("starting screenshot")
            screenshot.write(Some(robot.createScreenCapture(new Rectangle(0, 0, 2560, 1440))))
          case Some(image) =>
            logger.info(s"saving screenshot [$screenshotRect]")
            val area = screenshotRect()
            val folder = Files.createTempDirectory("bot-").toFile
            folder.mkdirs()
            val file = new File(s"${folder.getAbsolutePath}/${uuid}_${area.position.x}_${area.position.y}_${area.size.x}_${area.size.y}.png")
            file.createNewFile()
            ImageIO.write(image.getSubimage(area.position.x, area.position.y, area.size.x, area.size.y), "png", file)
            desktop.open(folder)
            screenshot.write(None)
        }
      }

    /** F3 - screenshot start location */
    case 61 =>
      screenshotRect.write(screenshotRect().positionAt(mouseLocation))
      logger.info(s"captured screenshot start at [$screenshotRect]")

    /** F4 - screenshot end location */
    case 62 =>
      screenshotRect.write(screenshotRect().resizeTo(mouseLocation - screenshotRect().position))
      logger.info(s"captured screenshot end at [$screenshotRect]")

    case code =>
      logger.info(s"keyboard [$code]")
  }

  val system = ActorSystem("poe")
  system.actorOf(Props(new Ticker(1.second)))

  /** Returns the current global mouse position */
  def mouseLocation: Vec2i = {
    val info = MouseInfo.getPointerInfo.getLocation
    info.x xy info.y
  }

  /** Moves mouse to a given global location */
  def moveMouse(location: Vec2i): Unit = {
    robot.mouseMove(location.x, location.y)
  }

  /** Clicks mouse at given location */
  def mouseClick(location: Vec2i): Unit = {
    val mouse = mouseLocation
    moveMouse(location)
    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
    Thread.sleep(1)
    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
    moveMouse(mouse)
  }

  /** Returns true if screen matches the given file contents */
  def screenMatches(file: String, threshold: Double = 0.8): Boolean = {
    val regex = raw".{36,36}_(\d+)_(\d+)_(\d+)_(\d+).png".r
    val area = file match {
      case regex(x, y, w, h) => Rec2i(x.toInt xy y.toInt, w.toInt xy h.toInt)
    }
    val mouse = mouseLocation
    moveMouse(Vec2i.Zero)
    val actual = robot.createScreenCapture(new Rectangle(area.position.x, area.position.y, area.size.x, area.size.y))
    moveMouse(mouse)
    val expected = ImageIO.read(new File(s"${templates.getAbsolutePath}/$file"))
    val count = (0 until area.size.x).map { x =>
      (0 until area.size.y).map { y =>
        if (expected.getRGB(x, y) == actual.getRGB(x, y)) 1 else 0
      }.sum
    }.sum
    val similarity = count.toDouble / (area.size.x * area.size.y)
    logger.info(s"screen similarity [$similarity] for [$file]")
    similarity > threshold
  }

  /** Returns true if game is maximized */
  def isMinimized: Boolean = screenMatches("0e256972-3ac8-4510-8ea1-3dd44de054ec_152_1405_149_28.png")

  /** Starts listening to keyboard events */
  def initKeyboardListener(): Unit = {
    LogManager.getLogManager.reset()
    val logger = Logger.getLogger(classOf[GlobalScreen].getPackage.getName)
    logger.setLevel(Level.OFF)
    GlobalScreen.registerNativeHook()
    GlobalScreen.addNativeKeyListener(new NativeKeyListener {
      override def nativeKeyPressed(nativeKeyEvent: NativeKeyEvent): Unit = keyboard.write(nativeKeyEvent.getKeyCode)

      override def nativeKeyReleased(nativeKeyEvent: NativeKeyEvent): Unit = {}

      override def nativeKeyTyped(nativeKeyEvent: NativeKeyEvent): Unit = {}
    })
  }

  class Ticker(tickDelay: FiniteDuration) extends Actor {
    implicit val ec: ExecutionContextExecutor = context.system.dispatcher

    override def preStart(): Unit = {
      context.system.scheduler.schedule(tickDelay, tickDelay, self, Tick)
    }

    override def receive: Receive = {
      case Tick => if (enabled()) tick.write(tick() + 1)
    }
  }

  object Tick

}