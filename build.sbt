name := "books-rabbitmq-alpakka"

version := "1.0"

scalaVersion := "2.12.3"

resolvers += "spray repo" at "http://repo.spray.io/"

libraryDependencies ++= Seq(
  "com.lightbend.akka" %% "akka-stream-alpakka-amqp" % "0.13",
  "com.typesafe.akka" %% "akka-http" % "10.0.9",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.9",
  "io.spray" %% "spray-json"   % "1.3.3",
  "com.typesafe" % "config" % "1.3.1"
)
