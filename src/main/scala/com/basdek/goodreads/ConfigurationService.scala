package com.basdek.goodreads

trait ConfigurationService {

  //TODO before production: authentication!
  //TODO it would be nice to not be restricted to the default port.
  protected def dbHost: String

  protected def dbDb: String

}
