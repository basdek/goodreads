package com.basdek.goodreads.services

trait ConfigService extends AbstractConfigurationService {
  override def dbHost: String = "localhost"
  override def dbDb: String = "thauris-goodreads"
}
