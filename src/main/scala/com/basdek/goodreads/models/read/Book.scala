package com.basdek.goodreads.models.read

import reactivemongo.bson.{BSONDocumentReader, BSONObjectID, Macros}

//TODO: _id is kind of ugly, but it saves us the time of writing our own reader structures
//TODO: someday we might implement a real ISBN check / validity
case class Book(_id: BSONObjectID, author: String, bookTitle: String, isbn: String)

object Book {
  implicit val reader: BSONDocumentReader[Book] = Macros.reader[Book]
}
