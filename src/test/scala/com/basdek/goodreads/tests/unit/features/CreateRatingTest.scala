package com.basdek.goodreads.tests.unit.features

import com.basdek.goodreads.features.rating.Create
import com.basdek.goodreads.services.ConnectionService
import org.scalatest.{AsyncFlatSpec, Matchers}

import scalaz.{-\/, \/-}

class CreateRatingTest extends AsyncFlatSpec with Matchers {

  private val feature = new Create
    with ConnectionService with TestConfigurationService

  behavior of "The Rating Create feature"

  //We should have more error granularity and have more specific tests, TODO

  it should "give an error for a non existing isbn" in {
    val future = feature.handle(Create.Command(
      "not.a.book", "kwant-de-bas", 1
    ))

    future map { result =>
      result match {
        case -\/(e) => e should not equal(null) //<-TODO: more elegant.
        case _ => fail()
      }
    }
  }

  it should "give an error for a non existing slug" in {
    val future = feature.handle(Create.Command(
      "1", "not-a-user", 1
    ))

    future map { result =>
      result match {
        case -\/(e) => e should not equal(null) //<-TODO: more elegant.
        case _ => fail()
      }
    }
  }

  it should "complain in case of a duplicate pair (user, book)" in {
    val future = feature.handle(Create.Command(
      "1", "kwant-de-bas", 1
    ))

    future map { result =>
      result match {
        case -\/(e) => e should not equal(null) //<-TODO: more elegant.
        case _ => fail("A duplicate was allowed. Is the unique index properly created?")
      }
    }
  }

  it should "properly write the rating" in {
    val future = feature.handle(Create.Command(
      "3", "ipsum-lorum", 1
    ))

    future map { result =>
      result match {
        case \/-(r) =>  {
          r.bookTitle should equal("Ecce Homo")
        }
        case _ => fail()
      }
    }

  }
}
