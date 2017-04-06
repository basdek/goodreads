package com.basdek.goodreads.tests.unit.features

import com.basdek.goodreads.features.book.GetRatingsByISBN
import com.basdek.goodreads.services.ConnectionService
import org.scalatest.{AsyncFlatSpec, Matchers}

import scalaz.{-\/, \/-}


class GetRatingsByISBNTest extends AsyncFlatSpec with Matchers {

  private val feature = new GetRatingsByISBN
    with ConnectionService with TestConfigurationService


  behavior of "The GetRatingsByISBN feature"

  it should "retrieve a book with it's ratings successfully" in {
    val future = feature.handle(new GetRatingsByISBN.Query("1"))
    future map { result => {
      result match {
        case \/-(r) => {
          r.bookTitle should equal("Thus spoke Zarathustra")
          r.ratings.length should equal(1)
          r.ratings.head.username should equal("Bas Kwant, de")
        }
        case _ => fail("Unexpected left sided disjunction.")
      }
    }
    }
  }

  it should "retrieve a book without ratings successfully" in {
    val future = feature.handle(new GetRatingsByISBN.Query("5"))
    future map { result => {
      result match {
        case \/-(r) => {
          r.bookTitle should equal("The black swan")
          r.ratings.length should equal(0)
        }
        case _ => fail("Unexpected left sided disjunction.")
      }
    }
    }
  }

  it should "correctly return an error for a non existing book" in {
    val future = feature.handle(new GetRatingsByISBN.Query("does.not.exist"))
    future map { result =>
      result match {
        case -\/(e) => {e should not equal(null)} //<- bit of a strange test, something more elegant? TODO
        case _ => fail("Unexpected right sided disjunction. What?!")
      }
    }
  }
}
