package com.basdek.goodreads.tests.unit.features

import com.basdek.goodreads.ConnectionService
import com.basdek.goodreads.features.book.GetRatingsByISBN
import org.scalatest.{AsyncFlatSpec, Matchers}


class GetRatingsByISBNTest extends AsyncFlatSpec with Matchers {

  private val feature = new GetRatingsByISBN
    with ConnectionService with TestConfigurationService


  behavior of "The GetRatingsByISBN feature"

  it should "retrieve a book with it's ratings successfully" in {
    val future = feature.handle(new GetRatingsByISBN.Query("1"))
    future map { result => {
      result.bookTitle should equal("Thus spoke Zarathustra")
      result.ratings.length should equal(1)
      result.ratings.head.username should equal("Bas Kwant, de")
    }
    }
  }

  it should "retrieve a book without ratings successfully" in {
    val future = feature.handle(new GetRatingsByISBN.Query("5"))
    future map { result => {
      result.bookTitle should equal("The black swan")
      result.ratings.length should equal(0)
    }
    }
  }
}
