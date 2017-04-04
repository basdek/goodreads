package com.basdek.goodreads.features.book

import com.basdek.goodreads.ConnectionService
import com.basdek.goodreads.features.book.GetRatingsByISBN._
import com.basdek.goodreads.models.read._
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.Future

class GetRatingsByISBN {
  this: ConnectionService =>

  def handle(q: Query): Future[Result] = {

    val bookQuery = BSONDocument("isbn" -> q.isbn)

    def ratingsQuery(id: BSONObjectID) = BSONDocument("book" -> id)

    def usersQuery(ids: List[BSONObjectID]) =
      BSONDocument("_id" -> BSONDocument("$in" -> ids))

    //Sorting so that we can just zip the two result sets together.
    val sortByReader = BSONDocument("reader" -> 1)
    val sortById = BSONDocument("_id" -> 1)

    for {
      db <- db()
      book <- db.collection[BSONCollection]("books").find(bookQuery).cursor[Book]().collect[List](1)
      if book.length == 1
      ratings <- db.collection[BSONCollection]("ratings").find(ratingsQuery(book.head._id)).sort(sortByReader).cursor[Rating]().collect[List]()

      //Can we prevent the execution of this?
      readers <- db.collection[BSONCollection]("readers").find(usersQuery(ratings.map(x => x.reader))).sort(sortById).cursor[Reader]().collect[List]()

    } yield Result(book.head.bookTitle, ratings zip readers map { x => RatingView(x) })

  }
}

object GetRatingsByISBN {

  sealed case class Query(isbn: String)

  case class RatingView(username: String, rating: Int)

  case class Result(bookTitle: String, ratings: List[RatingView])

  object RatingView {
    def apply(arg: (Rating, Reader)): RatingView = {
      RatingView(arg._2.username, arg._1.rating)
    }
  }

}
