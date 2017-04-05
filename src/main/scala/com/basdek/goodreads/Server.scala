package com.basdek.goodreads

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import com.basdek.goodreads.features.book.GetRatingsByISBN
import com.basdek.goodreads.features.rating.Create
import com.basdek.goodreads.features.reader.GetRatingsByUserSlug
import com.basdek.goodreads.services.ConnectionService
import spray.json.DefaultJsonProtocol

trait JsonSupport extends DefaultJsonProtocol {
  implicit val ratingByUserSlugFormat = jsonFormat2(GetRatingsByUserSlug.RatingView.apply)
  implicit val ratingByUserSlugRatingViewFormat = jsonFormat2(GetRatingsByUserSlug.Result)
  implicit val ratingByISBNRatingViewFormat = jsonFormat2(GetRatingsByISBN.RatingView.apply)
  implicit val ratingByISBNFormat = jsonFormat2(GetRatingsByISBN.Result)
}

object Server extends App with Directives with JsonSupport {
  implicit val system = ActorSystem("goodreads")
  implicit val materializer = ActorMaterializer()

  implicit val executionContext = system.dispatcher

  val host = "localhost"
  val port = 8000

  val getRatingsByISBN = new GetRatingsByISBN
    with ConnectionService with ConfigService
  val getRatingsByUserSlug = new GetRatingsByUserSlug
    with ConnectionService with ConfigService
  val createRating = new Create
    with ConnectionService with ConfigService

  val readersRoute = path("readers" / """([a-z\-]*)""".r / "ratings") {
    slug => {
      get {
        onSuccess(getRatingsByUserSlug.handle(GetRatingsByUserSlug.Query(slug))) {
          result => complete(result)
        }
      }
    }
  }

  val booksRoute = path("books" / """([1-9\-]*)""".r / "ratings") {
    isbn => {
      get {
        onSuccess(getRatingsByISBN.handle(GetRatingsByISBN.Query(isbn))) {
          result => complete(result)
        }
      }
    }
  }

  val ratingCreateRoute = path("readers" / """([a-z\-]*)""".r / "isbn" / """([1-9\-]*)""".r) {
    (slug, isbn) => {
      post {
        onSuccess(createRating.handle(Create.Command(isbn, slug, 5))) {
          result => complete(None)
        }
      }
    }
  }

  Http().bindAndHandle(readersRoute ~ booksRoute ~ ratingCreateRoute, host, port)

}
