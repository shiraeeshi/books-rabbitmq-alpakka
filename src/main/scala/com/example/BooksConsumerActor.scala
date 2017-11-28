package com.example

import akka.actor.{ Actor, ActorLogging, Props, OneForOneStrategy }
import akka.actor.SupervisorStrategy._
import scala.concurrent.duration._

object BooksConsumerActor {
  case object Init
  case object Ack
  case object Complete
}

class BooksConsumerActor extends Actor with ActorLogging {
  import BooksConsumerActor._

  val worker = context.actorOf(Props[BooksConsumerWorkerActor], "books-consumer-worker-actor")

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
    //case _ => Resume
    case _ =>
      println("resume worker from booksConsumer")
      //Resume
      Stop
  }

  def receive: Receive = {
    case _: Init.type =>
      sender ! Ack
    case msg: String =>
      worker forward msg
      sender ! Ack
  }
}

class BooksConsumerWorkerActor extends Actor with BooksHandler with ActorLogging {
  import BooksConsumerActor.Ack

  override def preStart(): Unit = println("book-consumer-worker actor started")
  override def postStop(): Unit = println("book-consumer-worker actor stopped")

  def receive: Receive = {
    case msg: String =>
      println(s">>> actor received a message: $msg")
      /*try {
        handleBookMessage(msg)
      } catch {
        case e =>
          sender ! Ack
          throw e
      }
      */
      handleBookMessage(msg)
  }

}
