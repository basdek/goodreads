name := "goodreads"
organization := "com.basdek"
version := "0.1.0-SNAPSHOT"
scalaVersion := "2.12.1"


libraryDependencies ++= List(
  "com.typesafe.akka" %% "akka-actor" % "2.4.17",
  "org.reactivemongo" % "reactivemongo_2.12" % "0.12.1",
  "org.slf4j" % "slf4j-simple" % "1.7.25",
  "com.typesafe.akka" %% "akka-http" % "10.0.5",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

