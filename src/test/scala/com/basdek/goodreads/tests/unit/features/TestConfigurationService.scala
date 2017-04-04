package com.basdek.goodreads.tests.unit.features

import com.basdek.goodreads.ConfigurationService

trait TestConfigurationService extends ConfigurationService {

  override protected def dbHost: String = "localhost"
  override protected def dbDb : String = "test"
}
