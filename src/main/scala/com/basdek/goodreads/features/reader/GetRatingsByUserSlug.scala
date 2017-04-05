package com.basdek.goodreads.features.reader

import com.basdek.goodreads.features.reader.GetRatingsByUserSlug._
import com.basdek.goodreads.models.read.{Book, Rating, Reader}
import com.basdek.goodreads.services.ConnectionService
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.Future

class GetRatingsByUserSlug {
  this: ConnectionService =>

  def handle(q: Query): Future[Result] = {

    val readerQuery = BSONDocument("slug" -> q.slug)

    def ratingsQuery(id: BSONObjectID) = BSONDocument("reader" -> id)

    def booksQuery(ids: List[BSONObjectID]) =
      BSONDocument("_id" -> BSONDocument("$in" -> ids))

    //Sorting so that we can perform our join by zipping.
    val sortByBook = BSONDocument("book" -> 1)
    val sortById = BSONDocument("_id" -> 1)

    for {
      db <- db()
      reader <- db.collection[BSONCollection]("readers").find(readerQuery).cursor[Reader]().collect[List](1)
      if reader.length == 1
      ratings <- db.collection[BSONCollection]("ratings").find(ratingsQuery(reader.head._id)).sort(sortByBook).cursor[Rating]().collect[List]()

      //Can we prevent execution of this?
      books <- db.collection[BSONCollection]("books").find(booksQuery(ratings.map(x => x.book))).sort(sortById).cursor[Book]().collect[List]()

      result = Result(reader.head.username, ratings zip books map {x => RatingView(x)})
    } yield result
  }
}

object GetRatingsByUserSlug {

  case class Query(slug: String)

  case class RatingView(bookTitle: String, rating: Int)

  case class Result(username: String, ratings: List[RatingView])

  object RatingView {
    def apply(arg: (Rating, Book)): RatingView = {
      RatingView(arg._2.bookTitle, arg._1.rating)
    }
  }

}
