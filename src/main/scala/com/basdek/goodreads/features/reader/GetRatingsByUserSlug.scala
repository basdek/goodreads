package com.basdek.goodreads.features.reader

import com.basdek.goodreads.features.reader.GetRatingsByUserSlug._
import com.basdek.goodreads.features.ChainUtils._
import com.basdek.goodreads.models.read.{Book, Rating, Reader}
import com.basdek.goodreads.services.ConnectionService
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.{ExecutionContext, Future}
import scalaz.{EitherT, \/}
import scalaz.Scalaz._

class GetRatingsByUserSlug {
  this: ConnectionService =>

  def handle(q: Query) (implicit ec: ExecutionContext): Future[Error \/ Result] = {

    val readerQuery = BSONDocument("slug" -> q.slug)
    val readerNotFoundError = new Error("Reader with slug " + q.slug + " does not exist.")

    def ratingsQuery(id: BSONObjectID) = BSONDocument("reader" -> id)

    def booksQuery(ids: List[BSONObjectID]) =
      BSONDocument("_id" -> BSONDocument("$in" -> ids))

    //Sorting so that we can perform our join by zipping.
    val sortByBook = BSONDocument("book" -> 1)
    val sortById = BSONDocument("_id" -> 1)

    val logicChain = for {
      db <- EitherT.right(db())
      reader <- EitherT(predicateInjector[List[Reader]](db.collection[BSONCollection]("readers").find(readerQuery).cursor[Reader]().collect[List](1), x => x.nonEmpty, readerNotFoundError))
      ratings <- EitherT.right(db.collection[BSONCollection]("ratings").find(ratingsQuery(reader.head._id)).sort(sortByBook).cursor[Rating]().collect[List]())
      books <- EitherT.right(db.collection[BSONCollection]("books").find(booksQuery(ratings.map(x => x.book))).sort(sortById).cursor[Book]().collect[List]())

      result = Result(reader.head.username, ratings zip books map {x => RatingView(x)})
    } yield result

    logicChain.run
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
