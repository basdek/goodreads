package com.basdek.goodreads.tests.unit.features

import com.basdek.goodreads.services.AbstractConfigurationService

trait TestConfigurationService extends AbstractConfigurationService {

  override protected def dbHost: String = "localhost"
  override protected def dbDb : String = "test"
}
