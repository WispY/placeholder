import java.io.{FileOutputStream, FileWriter}
import java.nio.file.Files
import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME
import java.time.{ZoneId, ZonedDateTime}

import scala.sys.process._
import scala.util.Try

name := "cross-project"
scalaVersion in ThisBuild := "2.12.8"

lazy val root = project.in(file(".")).
  aggregate(crossJS, crossJVM).
  settings(
    publish := {},
    publishLocal := {}
  )

lazy val cross = crossProject.in(file(".")).
  settings(
    name := "cross",
    version := "0.1",
    libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.12.8"
  ).
  jvmSettings(
    name := "jvm",

    resolvers += Resolver.jcenterRepo,

    libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.12.8",
    // logging
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3",
    libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
    // akka
    libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.22",
    libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.1.8",
    libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.22",
    // discord client
    libraryDependencies += "net.dv8tion" % "JDA" % "3.8.3_463",

    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test,
    libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.5.22" % Test,
    libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % "10.1.8" % Test,
    libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.22" % Test,
  ).
  jsSettings(
    name := "js",
    // calls main method when js file is loaded
    scalaJSUseMainModuleInitializer := true,
    // dom - basic dom operations lib
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2"
  )

lazy val crossJVM = cross.jvm
lazy val crossJS = cross.js

lazy val moveJS = taskKey[Unit]("moveJS")
lazy val pushJS = taskKey[Unit]("pushJS")

def copyFile(from: String, to: String): Unit = {
  val in = new File(from).toPath
  val out = new File(to)
  out.delete()
  out.createNewFile()
  val stream = new FileOutputStream(out)
  Files.copy(in, stream)
  Try(stream.close())
}

def moveFile(from: String, to: String): Unit = {
  copyFile(from, to)
  new File(from).delete()
}

def copyFolder(from: String, to: String): Unit = {
  Option(new File(from).listFiles())
    .getOrElse(Array())
    .foreach {
      case folder if folder.isDirectory => copyFolder(s"$from/${folder.getName}", s"$to/${folder.getName}")
      case file => copyFile(s"$from/${file.getName}", s"$to/${file.getName}")
    }
}

def writeFile(file: String, content: String): Unit = {
  val out = new File(file)
  val writer = new FileWriter(out)
  writer.write(content)
  writer.flush()
  Try(writer.close())
}

moveJS := {
  moveFile("./js/target/scala-2.12/js-fastopt.js", "./out/cross-fastopt.js")
  moveFile("./js/target/scala-2.12/js-fastopt.js.map", "./out/cross-fastopt.js.map")
}

pushJS := {
  writeFile("./out/timestamp.txt", ZonedDateTime.now(ZoneId.of("UTC")).format(ISO_ZONED_DATE_TIME))
  copyFolder("./out", "./deploy")
  val commands = List(
    // """cd ./out""",
    """git add .""",
    """git commit -m "js deployment"""",
    """git push"""
  )
  s"cmd /C ${commands.mkString(" & ")}".!
}