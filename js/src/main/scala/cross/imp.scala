package cross

import java.util.UUID

import cross.animation.{Animation, ChaseInOut, Delay, FadeIn, FadeOut, FlipIn, FlipOut, OffsetIn, OffsetOut, Parallel}
import cross.component.Component
import cross.config._
import cross.data._
import cross.global.GlobalContext
import cross.mvc._
import cross.pattern._
import cross.pixi._
import cross.spring.SpritePositionSpring
import cross.vec._
import org.scalajs.dom

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success, Try}

//noinspection LanguageFeature
object imp extends GlobalContext {
  /** Converts scala map into javascript object */
  implicit def mapToJs[A](map: Map[String, A]): js.Dictionary[A] = map.toJSDictionary

  /** Converts scala traversable into javascript array */
  implicit def collectionToJsArray[A](list: Traversable[A]): js.Array[A] = list.toJSArray

  /** Converts points into vectors */
  implicit def pointToVec(point: Point): Vec2d = point.x xy point.y

  /** Converts integer vector in double vector */
  implicit def vec2iToVec2d(v: Vec2i): Vec2d = Vec2d(v.x, v.y)

  /** Converts components to display objects */
  implicit def componentToDisplayObject[A <: Component](c: A): DisplayObject = c.toPixi

  /** On error, prints it's stacktrace to the console */
  def unsafe[A](message: String)(code: => A): A = Try(code) match {
    case Success(a) => a
    case Failure(error) =>
      dom.console.error(message)
      error.printStackTrace()
      throw error
  }

  /** Builds a container bound to screen center and scale */
  def centerStage(implicit controller: Controller): Container = new Container().bindScale(controller.model.scale).springToCenter

  /** Builds a container bound to screen top left corner and scale */
  def topLeftStage(implicit controller: Controller): Container = new Container().bindScale(controller.model.scale)

  /** Builds a container bound to screen top and scale */
  def topStage(implicit controller: Controller): Container = new Container().bindScale(controller.model.scale).springTo(0.5 xy 0)

  /** Builds a container bound to screen top right corner and scale */
  def topRightStage(implicit controller: Controller): Container = new Container().bindScale(controller.model.scale).springTo(1 xy 0)

  /** Builds a container bound to screen bottom left corner and scale */
  def bottomLeftStage(implicit controller: Controller): Container = new Container().bindScale(controller.model.scale).springTo(0 xy 1)

  /** Builds a container bound to screen bottom and scale */
  def bottomStage(implicit controller: Controller): Container = new Container().bindScale(controller.model.scale).springTo(0.5 xy 1)

  /** Builds a container bound to screen bottom right corner and scale */
  def bottomRightStage(implicit controller: Controller): Container = new Container().bindScale(controller.model.scale).springTo(1 xy 1)

  /** Builds a delay animation with given amount of time */
  def delay(time: FiniteDuration = AnimationDelay): Animation = Delay(time)

  /** Randomizes the number between two given ones */
  def randomBetween(start: Double, end: Double): Double = start + Math.random() * (end - start)

  /** Applies the given code in next anumation frame */
  def nextFrame(code: => Unit): Unit = dom.window.requestAnimationFrame(_ => code)

  implicit class JsMapOps(val map: Map[String, js.Any]) extends AnyVal {
    /** Converts map to javascript object */
    def asJs: js.Dictionary[js.Any] = mapToJs(map)
  }

  implicit class DisplayObjectOps[A <: DisplayObject](val a: A) extends AnyVal {
    /** Randomizes the uuid of the object if not already assigned */
    def ensureUuid: A = a.mutate { a => if (!a.hasOwnProperty("uuid")) a.uuid = UUID.randomUUID().toString }

    /** Sets the z to -1 if not already assigned */
    def ensureZ: A = a.mutate { a => if (!a.hasOwnProperty("z")) a.z = -1 }

    /** Returns true if the object has not parent */
    def detached: Boolean = Option(a.parent).isEmpty

    /** Removes the object from it's parent */
    def detach: A = a.mutate { a => if (!detached) a.parent.removeChild(a) }

    /** Changes the width and height to the given size vector */
    def resizeTo(size: Vec2d): A = a.mutate { a =>
      a.width = size.x
      a.height = size.y
    }

    /** Changes the scale to the given value */
    def scaleTo(scale: Double): A = a.mutate { a => a.scale.set(scale, scale) }

    /** Changes the scale to the given value */
    def scaleTo(scale: Vec2d): A = a.mutate { a => a.scale.set(scale) }

    /** Changes the X scale to the given value */
    def scaleXTo(scale: Double): A = a.mutate { a => a.scale.x = scale }

    /** Changes the Y scale to the given value */
    def scaleYTo(scale: Double): A = a.mutate { a => a.scale.y = scale }

    /** Changes the X slew to the given value */
    def skewXTo(scale: Double): A = a.mutate { a => a.skew.x = scale }

    /** Changes the Y slew to the given value */
    def skewYTo(scale: Double): A = a.mutate { a => a.skew.y = scale }

    /** Changes the alpha to given value */
    def alphaAt(alpha: Double): A = a.mutate { a => a.alpha = alpha }

    /** Changes the position to the given location */
    def positionAt(position: Vec2d): A = a.mutate { a => a.position.set(position) }

    /** Changes the anchor location to the center of sprite */
    def anchorAtCenter: A = a.anchorAt(Vec2d.Center)

    /** Changes the anchor location to a given value */
    def anchorAt(anchor: Vec2d): A = a.mutate { a => a.anchor.set(anchor) }

    /** Changes the pivot location to a given value */
    def pivotAt(pivot: Vec2d): A = a.mutate { a => a.pivot.set(pivot) }

    /** Changes the rotation to a given value */
    def rotateTo(rotation: Double): A = a.mutate { a => a.rotation = rotation }

    /** Adds as a child to the given container */
    def addTo(parent: Container): A = a.mutate { a => parent.addChild(a) }

    /** Changes the visibility to a given value */
    def visibleTo(visible: Boolean): A = a.mutate { a => a.visible = visible }

    /** Changes the interactivity to a given value */
    def interactiveTo(interactive: Boolean): A = a.mutate { a => a.interactive = true }

    /** Adds the given object as mask */
    def maskWith(mask: => DisplayObject): A = a.mutate { a => a.mask = mask }

    /** Changes the filters of the object */
    def filterWith(filters: List[Filter]): A = a.mutate { a => a.filters = filters }

    /** Binds the location to given place */
    def springTo(target: Vec2d)(implicit controller: Controller): A = a.mutate { a =>
      a.positionAt(controller.model.screen() * target)
      val s = SpritePositionSpring(a)
      controller.model.screen /> { case size => s.target = size * target }
      spring.add(s)
    }

    /** Binds the location to screen center */
    def springToCenter(implicit controller: Controller): A = a.springTo(Vec2d.Center)

    /** Binds the scale to a given bind */
    def bindScale(data: Data[Double]): A = a.mutate { a => data /> { case scale => a.scaleTo(scale) } }

    /** Returns the absolute position of the object in the world */
    def absolutePosition: Vec2d = a.worldTransform.transform(m => m.tx xy m.ty)

    /** Returns the absolute rotation of the object as a sum of parent rotations */
    def absoluteRotation: Double = a.parentList.map(p => p.rotation).sum + a.rotation

    /** Returns the absolute scale of the object as a multiplication of parent scales */
    def absoluteScale: Vec2d = a.parentList.foldLeft(pointToVec(a.scale)) { case (scale, parent) => scale * parent.scale }

    /** Returns a list of all parents up to the root */
    def parentList: List[DisplayObject] = {
      @tailrec
      def rec(parents: List[DisplayObject], current: DisplayObject): List[DisplayObject] = Option(current.parent) match {
        case None => parents
        case Some(parent) => rec(parents :+ parent, parent)
      }

      rec(Nil, a)
    }

    /** Moves, scales and rotates the object to given anchor immediately */
    def warpTo(anchor: DisplayObject): A = a.mutate { a => a.positionAt(anchor.absolutePosition).scaleTo(anchor.absoluteScale).rotateTo(anchor.absoluteRotation) }

    def fadeIn: FadeIn = FadeIn(a)

    def fadeOut: FadeOut = FadeOut(a)

    def flipIn: FlipIn = FlipIn(a)

    def flipOut: FlipOut = FlipOut(a)

    def offsetIn(original: Vec2d, offset: Vec2d): OffsetIn = OffsetIn(a, original, offset)

    def offsetOut(original: Vec2d, offset: Vec2d): OffsetOut = OffsetOut(a, original, offset)

    def chase(source: DisplayObject, target: DisplayObject): ChaseInOut = ChaseInOut(a, source, target)
  }

  implicit class ContainerOps(val a: Container) extends AnyVal {
    /** Builds a new sub-container */
    def sub: Container = new Container().addTo(a)

    /** Removes all children from container */
    def removeChildren: Container = a.mutate { a => while (a.children.nonEmpty) a.removeChild(a.children.head) }
  }

  implicit class AssetOps(val a: Asset) extends AnyVal {
    /** Returns the texture from the loader */
    def texture(implicit app: Application): BaseTexture = app.loader.resources(a.path).texture

    /** Returns a new Sprite for this asset */
    def sprite(implicit app: Application): Sprite = new Sprite(a.texture).ensureUuid
  }

  implicit class AssetListOps(val l: List[Asset]) extends AnyVal {
    /** Resets the loader and loads a list of given assets */
    def resetAndAddToLoader(implicit app: Application): Loader = app.loader.reset().add(l.map(a => a.path).distinct)
  }

  implicit class PointOps(val p: Point) extends AnyVal {
    /** Sets the point values to vector fields */
    def set(vec: Vec2d): Unit = p.set(vec.x, vec.y)
  }

  implicit class DoubleOps(val double: Double) extends AnyVal {
    /** Formats the given double with given number of digits after point */
    def pretty(digits: Int = 2): String = String.format(s"%.${digits}f", double: java.lang.Double)

    /** Normalizes the rotation by removing extra PIs */
    def normalRotation: Double = {
      if (double > Math.PI || double < -Math.PI) double - Math.PI * (double / Math.PI).toInt
      else double
    }

    /** Converts value to radians */
    def rad: Double = double / 180 * Math.PI
  }

  implicit class DoubleTupleOps(val tuple: (Double, Double)) extends AnyVal {
    /** Returns a number between two components according to progress */
    def %%(progress: Double): Double = tuple._1 + (tuple._2 - tuple._1) * progress

    /** Returns a number either at the end of the range or between the range */
    def %%%(range: (Double, Double), current: Double): Double = {
      if (current <= range._1) {
        tuple._1
      } else if (current >= range._2) {
        tuple._2
      } else {
        tuple %% ((current - range._1) / (range._2 - range._1))
      }
    }

    /** Returns a rotation between two components according to progress, normalizes the components */
    def rotationProgress(progress: Double): Double = {
      val (a, b) = tuple
      val (an, bn) = (a.normalRotation, b.normalRotation)
      val diff = List(bn - an, bn + Math.PI * 2 - an, bn - Math.PI * 2 - an).minBy(diff => diff.abs)
      (a, a + diff) %% progress
    }
  }

  implicit class TextStyleOps(val style: TextStyle) extends AnyVal {
    /** Creates a text object from given style */
    def text(text: String): Text = new Text(text, style)
  }

  implicit class AnimationListOps(val list: List[Animation]) extends AnyVal {
    /** Executes animations in parallel */
    def parallel: Animation = Parallel(list)
  }

  implicit class GraphicsOps(val graphics: Graphics) extends AnyVal {
    /** Draws a rectangle with given size */
    def fillRect(size: Vec2d, color: Double = 0): Graphics = {
      val (w, h) = (size.x, size.y)
      graphics
        .beginFill(color)
        .moveTo(0, 0)
        .lineTo(w, 0)
        .lineTo(w, h)
        .lineTo(0, h)
        .lineTo(0, 0)
        .endFill()
    }

    /** Draws a rectangle with rounded corners */
    def fillRoundRect(size: Vec2d, radius: Double, color: Double = 0): Graphics = {
      val (w, h, r) = (size.x, size.y, radius)
      graphics
        .beginFill(color)
        .moveTo(r, 0)
        .lineTo(w - r, 0).quadraticCurveTo(w, 0, w, r)
        .lineTo(w, h - r).quadraticCurveTo(w, h, w - r, h)
        .lineTo(r, h).quadraticCurveTo(0, h, 0, h - r)
        .lineTo(0, r).quadraticCurveTo(0, 0, r, 0)
        .endFill()
    }

    /** Draws a rectangle with cut corners */
    def fillCutRect(size: Vec2d, cut: Double, color: Double = 0): Graphics = {
      val (w, h, c) = (size.x, size.y, cut)
      graphics
        .beginFill(color)
        .moveTo(c, 0)
        .lineTo(w - c, 0)
        .lineTo(w, c)
        .lineTo(w, h - c)
        .lineTo(w - c, h)
        .lineTo(c, h)
        .lineTo(0, h - c)
        .lineTo(0, c)
        .lineTo(c, 0)
        .endFill()
    }
  }

  implicit class FiniteDurationOps(val duration: FiniteDuration) extends AnyVal {
    /** Multiplies the duration by given number */
    def **(multiplier: Double): FiniteDuration = (duration.toMillis * multiplier).toLong.millis
  }

  implicit class DataOptionOps[A](val data: Writable[Option[A]]) extends AnyVal {
    /** Sets optional value to Some given value */
    def writeSome(a: A): Unit = data.write(Some(a))

    /** Updates the optional value, if present */
    def update(code: A => A): Unit = {
      val current = data.read
      val next = current.map(code)
      next.foreach(v => data.write(Some(v)))
    }
  }

  implicit class DataOptionOptionOps[A](val data: Data[Option[Option[A]]]) extends AnyVal {
    /** Unwraps the inner option for double option data */
    def flatten: Data[Option[A]] = data /~ { case Some(Some(a)) => a }
  }

  implicit class FutureOps[A](val future: Future[A]) extends AnyVal {
    /** Clears the future value into unit */
    def clear: Future[Unit] = future.map(a => ())

    /** Appends the next future without data dependency */
    def >>[B](other: Future[B]): Future[Unit] = future.flatMap(any => other).clear
  }

  implicit class MapOps[A, B](val map: Map[A, B]) extends AnyVal {
    /** Ensures that value exists in mutable map reference */
    def ensure(key: A, rewrite: Map[A, B] => Unit)(code: => B): B = {
      map.get(key) match {
        case Some(value) => value
        case None =>
          val value = code
          rewrite.apply(map + (key -> value))
          value
      }
    }
  }

  implicit class TraversableOps[A](val list: Traversable[A]) extends AnyVal {
    /** Safely calculates min for non-empty seqs */
    def minOpt(implicit ordering: Ordering[A]): Option[A] = list match {
      case empty if empty.isEmpty => None
      case values => Some(values.min)
    }

    /** Safely calculates max for non-empty seqs */
    def maxOpt(implicit ordering: Ordering[A]): Option[A] = list match {
      case empty if empty.isEmpty => None
      case values => Some(values.max)
    }
  }

  implicit class ListVec2iOps(val list: Traversable[Vec2i]) extends AnyVal {
    /** Returns the min to max range of X values */
    def rangeX: Option[Vec2i] = list match {
      case empty if empty.isEmpty => None
      case nonempty =>
        val xs = list.map(v => v.x)
        Some(xs.min xy xs.max)
    }

    /** Returns the min to max range of Y values */
    def rangeY: Option[Vec2i] = list match {
      case empty if empty.isEmpty => None
      case nonempty =>
        val ys = list.map(v => v.y)
        Some(ys.min xy ys.max)
    }
  }

}