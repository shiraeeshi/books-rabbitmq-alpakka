package com.example

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import com.typesafe.config.ConfigFactory

import akka.actor.{ ActorSystem, Props, DeadLetter }
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.alpakka.amqp.{AmqpConnectionUri, IncomingMessage, NamedQueueSourceSettings, QueueDeclaration}
import akka.stream.alpakka.amqp.javadsl.AmqpSource
import akka.stream.scaladsl._
import akka.util.ByteString

object BooksConsumer extends App {
  import BooksConsumerActor._

  implicit val actorSystem = ActorSystem()
  implicit val actorMaterializer = ActorMaterializer()

  val deadLetterMonitorActor = actorSystem.actorOf(Props[DeadLetterMonitorActor], "DeADleTTerMoniToR-actor")
  actorSystem.eventStream.subscribe(deadLetterMonitorActor, classOf[DeadLetter])

  private val configFactory = ConfigFactory.load()

  val host = configFactory.getString("custom.rabbitmq.host")
  val port = configFactory.getInt("custom.rabbitmq.port")

  val queueName = "booksQueue"
  val queueDeclaration = QueueDeclaration(queueName, durable = true)
  val uri = s"amqp://guest:guest@$host:$port/my-rabbit"
  val amqpUri = AmqpConnectionUri(uri)
  val namedQueueSourceSettings = NamedQueueSourceSettings(amqpUri, queueName).withDeclarations(queueDeclaration)
  val source = AmqpSource.atMostOnceSource(namedQueueSourceSettings, bufferSize = 10)
  val flow1 = Flow[IncomingMessage].map(msg => msg.bytes)
  val flow2 = Flow[ByteString].map(_.utf8String)
  //val sink = Sink.foreach[String](println)
  val booksConsumerActor = actorSystem.actorOf(Props[BooksConsumerActor], "books-consumer-actor")
  val sink = Sink.actorRefWithAck(booksConsumerActor, Init, Ack, Complete)
  val graph = RunnableGraph.fromGraph(GraphDSL.create(sink) { implicit builder => s =>
    import GraphDSL.Implicits._
    source ~> flow1 ~> flow2 ~> s.in
    ClosedShape
  })
  /*
  val future = graph.run()
  future onComplete { _ =>
    actorSystem.terminate()
  }
  Await.result(actorSystem.whenTerminated, Duration.Inf)
  */
  graph.run()

}
