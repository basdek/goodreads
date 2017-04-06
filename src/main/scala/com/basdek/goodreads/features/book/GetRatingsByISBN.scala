package com.basdek.goodreads.features.book

import com.basdek.goodreads.features.book.GetRatingsByISBN._
import com.basdek.goodreads.models.read._
import com.basdek.goodreads.services.ConnectionService
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import com.basdek.goodreads.features.ChainUtils._
import scala.concurrent.{ExecutionContext, Future}
import scalaz.{EitherT, \/}
import scalaz.Scalaz._


class GetRatingsByISBN {
  this: ConnectionService =>

  def handle(q: Query) (implicit ec: ExecutionContext): Future[Error \/ Result] = {

    val bookQuery = BSONDocument("isbn" -> q.isbn)
    val bookNotFoundError : Error = new Error("Book with ISBN " + q.isbn + " not found.")

    def ratingsQuery(id: BSONObjectID) = BSONDocument("book" -> id)

    def usersQuery(ids: List[BSONObjectID]) =
      BSONDocument("_id" -> BSONDocument("$in" -> ids))

    //Sorting so that we can just zip the two result sets together.
    val sortByReader = BSONDocument("reader" -> 1)
    val sortById = BSONDocument("_id" -> 1)

    val logicChain = for {
      db <- EitherT.right(db())
      book <- EitherT(predicateInjector[List[Book]](db.collection[BSONCollection]("books").find(bookQuery).cursor[Book]().collect[List](1), x => x.nonEmpty, bookNotFoundError))
      ratings <- EitherT.right(db.collection[BSONCollection]("ratings").find(ratingsQuery(book.head._id)).sort(sortByReader).cursor[Rating]().collect[List]())
      readers <- EitherT.right(db.collection[BSONCollection] ("readers").find (usersQuery (ratings.map (x => x.reader))).sort (sortById).cursor[Reader] ().collect[List] (ratings.length))
      result = Result(book.head.bookTitle, ratings zip readers map {x => RatingView(x)})
    } yield result

    logicChain.run
  }

}

object GetRatingsByISBN {

  case class Query(isbn: String)

  case class RatingView(username: String, rating: Int)

  case class Result(bookTitle: String, ratings: List[RatingView])

  object RatingView {
    def apply(arg: (Rating, Reader)): RatingView = {
      RatingView(arg._2.username, arg._1.rating)
    }
  }

}
