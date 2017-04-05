package com.basdek.goodreads.tests.unit.features

import com.basdek.goodreads.features.reader.GetRatingsByUserSlug
import com.basdek.goodreads.services.ConnectionService
import org.scalatest.{AsyncFlatSpec, Matchers}

class GetRatingsByUserSlugTest extends AsyncFlatSpec with Matchers {

  private val feature = new GetRatingsByUserSlug
    with ConnectionService with TestConfigurationService

  behavior of "The GetRatingsByUserSlug feature"

  it should "retrieve a user without ratings successfully" in {
    val future = feature.handle(new GetRatingsByUserSlug.Query("nothing-read"))
    future map { result => {
      result.username should equal("Read Nothing")
      }
    }
  }

  it should "retrieve a user with it's ratings" in {
    val future = feature.handle(new GetRatingsByUserSlug.Query("kwant-de-bas"))
    future map { result => {
      result.username should equal("Bas Kwant, de")
      (result.ratings.length > 1) should equal(true)
    }
    }
  }


}
