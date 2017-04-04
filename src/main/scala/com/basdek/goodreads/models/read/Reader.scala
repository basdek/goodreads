package com.basdek.goodreads.models.read

import reactivemongo.bson.{BSONDocumentReader, BSONObjectID, Macros}

//TODO: _id is ugly, see other read models.
case class Reader(_id: BSONObjectID, name: String, surname: String, slug: String) {
  def username: String = name + " " + surname
}

object Reader {
  implicit val reader: BSONDocumentReader[Reader] = Macros.reader[Reader]
}
