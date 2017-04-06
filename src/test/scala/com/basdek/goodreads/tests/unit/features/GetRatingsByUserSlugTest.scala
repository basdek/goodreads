package com.basdek.goodreads.tests.unit.features

import com.basdek.goodreads.features.reader.GetRatingsByUserSlug
import com.basdek.goodreads.services.ConnectionService
import org.scalatest.{AsyncFlatSpec, Matchers}

import scalaz.{-\/, \/-}

class GetRatingsByUserSlugTest extends AsyncFlatSpec with Matchers {

  private val feature = new GetRatingsByUserSlug
    with ConnectionService with TestConfigurationService

  behavior of "The GetRatingsByUserSlug feature"

  it should "retrieve a user without ratings successfully" in {
    val future = feature.handle(new GetRatingsByUserSlug.Query("nothing-read"))
    future map { result =>
      result match {
        case \/-(r) => {
          r.username should equal("Read Nothing")
        }
        case _ => fail("Unexpected left sided projection.")
      }
    }
  }

  it should "retrieve a user with it's ratings" in {
    val future = feature.handle(new GetRatingsByUserSlug.Query("kwant-de-bas"))
    future map { result => {
      result match {
        case \/-(r) => {
          r.username should equal("Bas Kwant, de")
          (r.ratings.length > 1) should equal(true)
        }
        case _ => fail("Unexpected left sided projection.")
      }
    }
    }
  }

  it should "result in an error for a user slug that does not exist" in {
    val future = feature.handle(new GetRatingsByUserSlug.Query("does-not-exist"))
    future map {result => {
      result match {
        case -\/(e) => e should not equal(null) // There must be a better way. TODO
        case _ => fail("Unexpected right sided disjunction.")
      }
    }
    }
  }


}
