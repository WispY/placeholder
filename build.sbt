name := "Placeholder root project"

scalaVersion in ThisBuild := "2.12.4"

lazy val root = project.in(file(".")).
  aggregate(crossJS, crossJVM).
  settings(
    publish := {},
    publishLocal := {}
  )

lazy val cross = crossProject.in(file(".")).
  settings(
    name := "foo",
    version := "0.1"
  ).
  jvmSettings(
    // Add JVM-specific settings here
  ).
  jsSettings(
    // Add JS-specific settings here
  )

lazy val crossJVM = cross.jvm
lazy val crossJS = cross.js
