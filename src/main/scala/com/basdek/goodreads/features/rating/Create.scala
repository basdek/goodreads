package com.basdek.goodreads.features.rating

import com.basdek.goodreads.features.ChainUtils._
import com.basdek.goodreads.features.rating.Create._
import com.basdek.goodreads.models.read.{Book, Reader}
import com.basdek.goodreads.services.ConnectionService
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.{ExecutionContext, Future}
import scalaz.{EitherT, \/}
import scalaz.Scalaz._

class Create() {
  this: ConnectionService =>

  def handle(c: Command) (implicit ec: ExecutionContext): Future[Error \/ Result] = {
    val bookQuery = BSONDocument("isbn" -> c.isbn)
    val bookNotFoundError = new Error("Book with ISBN " + c.isbn + " does not exist.")

    val readerQuery = BSONDocument("slug" -> c.userSlug)
    val readerNotFoundError = new Error("User with slug " + c.userSlug + " does not exist.")

    def makeDoc(bookId: BSONObjectID, readerId: BSONObjectID) : BSONDocument =
      BSONDocument(
        "book" -> bookId,
        "reader" -> readerId,
        "rating" -> c.rating
      )

    val logicChain = for {
      db <- EitherT.right(db())
      books <- EitherT(predicateInjector[List[Book]](db.collection[BSONCollection]("books").find(bookQuery).cursor[Book]().collect[List](1), x => x.nonEmpty, bookNotFoundError))
      book = books.head
      readers <- EitherT(predicateInjector[List[Reader]](db.collection[BSONCollection]("readers").find(readerQuery).cursor[Reader]().collect[List](1), x => x.nonEmpty, readerNotFoundError))
      reader = readers.head
      writer <- EitherT(writeErrorPropagator(db.collection[BSONCollection]("ratings").insert(makeDoc(book._id, reader._id))))
      result = Result(reader.username, book.bookTitle, c.rating)
    } yield result

    logicChain.run

  }


}

object Create {
  case class Command(isbn: String, userSlug: String, rating: Int)

  case class Result(username: String, bookTitle: String, rating: Int)
}
