package com.example

import scala.util.{Success, Failure, Random}

import akka.actor.{ Actor, Props }

import com.typesafe.config.ConfigFactory

import spray.json._
import DefaultJsonProtocol._

import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer

import scala.concurrent.Future

trait BooksHandler extends DefaultJsonProtocol with SprayJsonSupport { this: Actor =>
  val system = context.system
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val http = Http(system)

  private val configFactory = ConfigFactory.load()

  val host = configFactory.getString("custom.books-service.host")
  val port = configFactory.getInt("custom.books-service.port")

  val random = new Random()

  def handleBookMessage(msg: String): Unit = {
    if (random.nextDouble > 0.5) {
      throw new IllegalStateException("random error thrown to simulate real world application errors")
    } else {
      println("random error was not thrown")
    }
    val futureResponse: Future[HttpResponse] =
      Marshal(msg).to[RequestEntity] flatMap { entity =>
        val request = HttpRequest(
          method = HttpMethods.POST,
          uri = s"http://$host:$port/api/books"
        ).withEntity(ContentTypes.`application/json`, msg)
        http.singleRequest(request)
      }
    
    futureResponse onComplete {
      case Success(response) =>
        println(s" response for POST request to books-service: ${response.entity}")
      case Failure(error) =>
        println(s"Couldn't post to books-service: $error")
    }
  }
}
