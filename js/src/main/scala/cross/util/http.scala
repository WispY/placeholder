package cross.util

import java.net.URI
import java.util.Base64

import cross.binary._
import cross.general.config.GeneralConfig
import cross.util.logging.Logging
import org.scalajs.dom.{XMLHttpRequest, document, window}

import scala.concurrent.{Future, Promise}
import scala.scalajs.js.URIUtils
import scala.util.Try

object http extends Logging {
  override protected def logKey: String = "http"

  log.info("0")

  /** Performs the get http request */
  def get[A](path: String, parameters: List[(String, Any)] = Nil)(implicit config: GeneralConfig, aformat: BF[A]): Future[A] = {
    request("GET", path, parameters, None, response = true)(config, unitFormat, aformat)
  }

  /** Performs the post http request */
  def post[A, B](path: String, body: A)(implicit config: GeneralConfig, aformat: BF[A], bformat: BF[B]): Future[B] = {
    request("POST", path, Nil, Some(body), response = true)(config, aformat, bformat)
  }

  /** Performs the post http request without parsing response */
  def postUnit[A](path: String, body: A)(implicit config: GeneralConfig, aformat: BF[A]): Future[Unit] = {
    request("POST", path, Nil, Some(body), response = false)(config, aformat, unitFormat)
  }

  /** Redirects to given path within same server */
  def redirect(path: String)(implicit config: GeneralConfig): Unit = {
    window.location.href = s"${config.client}$path"
  }

  /** Redirects to given uri */
  def redirectFull(uri: String): Unit = {
    window.location.href = uri
  }

  /** Redirects to given url without reloading the page */
  def redirectSilent(path: String, preserveQuery: Boolean = true): Unit = {
    val fullPath = if (preserveQuery) {
      val query = queryString
      if (query.isEmpty) path else s"$path?$query"
    } else path
    window.history.pushState(scalajs.js.Object(), "", fullPath)
  }

  /** Updates the page title */
  def updateTitle(title: String): Unit = {
    document.title = title
  }

  /** Preforms the http request to given uri */
  def request[A, B](method: String, path: String, parameters: List[(String, Any)], body: Option[A], response: Boolean)(implicit config: GeneralConfig, aformat: BF[A], bformat: BF[B]): Future[B] = {
    val query = if (parameters.isEmpty) "" else {
      val string = parameters
        .map { case (key, value) => s"${encode(key)}=${encode(value)}" }
        .mkString("&")
      s"?$string"
    }

    val requestBase64 = body.map { a =>
      val bytes = a.toBinary.toByteArray
      Base64.getEncoder.encodeToString(bytes)
    }

    val promise = Promise[B]()

    val request = new XMLHttpRequest()
    request.withCredentials = true
    request.onload = { _ =>
      if (response) {
        promise.complete(Try {
          val base64 = request.response.asInstanceOf[String]
          val bytes = Base64.getDecoder.decode(base64)
          ByteList(bytes).toScala[B]()
        })
      } else {
        promise.success(().asInstanceOf[B])
      }
    }
    request.onerror = { _ =>
      val message = s"failed http request [$method $path] with status [${request.status} - ${request.statusText}]"
      val up = new RuntimeException(message)
      log.error(message, up)
      promise.failure(up)
    }
    request.open(method, s"${config.server}$path$query", async = true)
    request.responseType = "text"
    request.setRequestHeader("Content-Type", "text/plain")
    requestBase64 match {
      case Some(base64) => request.send(base64)
      case None => request.send()
    }

    promise.future
  }

  /** URL encodes the given string */
  def encode(value: Any): String = URIUtils.encodeURIComponent(value.toString)

  /** Returns the server url */
  def hostPortString: String = {
    val uri = URI.create(window.location.href)
    uri.getPort match {
      case -1 => s"${uri.getScheme}://${uri.getHost}"
      case port => s"${uri.getScheme}://${uri.getHost}:$port"
    }
  }

  /** Returns the full query string */
  def queryString: String = Option(URI.create(window.location.href).getQuery).getOrElse("")

  /** Returns the query parameter map from current URL */
  def queryParameters: Map[String, List[String]] = {
    queryString.split('&')
      .map(URIUtils.decodeURIComponent)
      .map { pair =>
        val split = pair.split('=')
        (split.head, split.tail.mkString("="))
      }
      .groupBy { case (key, value) => key }
      .map { case (key, values) => key -> values.toList.map { case (k, v) => v } }
  }

  /** Returns the first value of a query parameter with given name */
  def queryParameter(name: String): Option[String] = {
    queryParameters.get(name).flatMap(values => values.headOption)
  }
}