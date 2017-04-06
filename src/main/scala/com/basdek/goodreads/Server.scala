package com.basdek.goodreads

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import com.basdek.goodreads.features.book.GetRatingsByISBN
import com.basdek.goodreads.features.rating.Create
import com.basdek.goodreads.features.reader.GetRatingsByUserSlug
import com.basdek.goodreads.services.ConnectionService
import spray.json.DefaultJsonProtocol

import scalaz.{-\/, \/-}

case class RatingRequest(rating: Int)

trait JsonSupport extends DefaultJsonProtocol {
  implicit val ratingByUserSlugFormat = jsonFormat2(GetRatingsByUserSlug.RatingView.apply)
  implicit val ratingByUserSlugRatingViewFormat = jsonFormat2(GetRatingsByUserSlug.Result)
  implicit val ratingByISBNRatingViewFormat = jsonFormat2(GetRatingsByISBN.RatingView.apply)
  implicit val ratingByISBNFormat = jsonFormat2(GetRatingsByISBN.Result)
  implicit val createdRatingFormat = jsonFormat3(Create.Result)

  implicit val ratingRequest = jsonFormat1(RatingRequest)
}

object Server extends App with Directives with JsonSupport {
  implicit val system = ActorSystem("goodreads")
  implicit val materializer = ActorMaterializer()

  implicit val executionContext = system.dispatcher

  val host = "localhost"
  val port = 8000

  //This is probably a bit wasteful, please recheck this pattern. TODO
  val getRatingsByISBN = new GetRatingsByISBN
    with ConnectionService with ConfigService
  val getRatingsByUserSlug = new GetRatingsByUserSlug
    with ConnectionService with ConfigService
  val createRating = new Create
    with ConnectionService with ConfigService

  val booksRoute = path("books" / """([1-9\-]*)""".r / "ratings") {
    isbn => {
      get {
        //onSuccess is a misleading name, our future can contain an error.
        onSuccess(getRatingsByISBN.handle(GetRatingsByISBN.Query(isbn))) {
          res => {
            res match {
              //TODO this is a bit ugly, but there is something odd when e is of type Error.
              case -\/(e) => complete(StatusCodes.NotFound, e.toString)

              case \/-(r) => complete(r)
            }
          }
        }
      }
    }
  }

  val readersRoute = path("readers" / """([a-z\-]*)""".r / "ratings") {
    slug => {
      get {
        onSuccess(getRatingsByUserSlug.handle(GetRatingsByUserSlug.Query(slug))) {
          res => {
            res match {
              case -\/(e) => complete(StatusCodes.NotFound, e.toString) //Inelegant.
              case \/-(r) => complete(r)
            }
          }
        }
      }
    }
  }

  val ratingCreateRoute = path("readers" / """([a-z\-]*)""".r / "isbn" / """([1-9\-]*)""".r) {
    (slug, isbn) => {
      post {
        entity(as[RatingRequest]) { body =>
          //TODO: maybe some proper validation lib someday?
          if (0 > body.rating || body.rating > 5) {
            complete(StatusCodes.BadRequest, "Ratings should be between 1 and 5")
          }
          else {
            onSuccess(createRating.handle(Create.Command(isbn, slug, body.rating))) {
              res => {
                res match {
                  case -\/(e) => complete(StatusCodes.BadRequest, e.toString)
                  case \/-(r) => complete(r)
                }
              }
            }
          }
        }
      }
    }
  }

  Http().bindAndHandle(readersRoute  ~ booksRoute ~ ratingCreateRoute, host, port)

}
