package com.basdek.goodreads.models.read

import reactivemongo.bson.{BSONDocumentReader, BSONObjectID, Macros}

//TODO: _id, see Book read model.
case class Rating(_id: BSONObjectID,
                  rating: Int,
                  book: BSONObjectID,
                  reader: BSONObjectID
                 )

object Rating {
  implicit val reader: BSONDocumentReader[Rating] = Macros.reader[Rating]
}

