package com.basdek.goodreads.models.write

import reactivemongo.bson.BSONObjectID

//_id, still hairy, TODO
case class Rating(_id: Option[BSONObjectID], rating: Int, book: BSONObjectID, reader: BSONObjectID)
