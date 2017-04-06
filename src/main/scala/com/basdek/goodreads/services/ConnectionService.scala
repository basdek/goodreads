package com.basdek.goodreads.services

import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}

import scala.concurrent.{ExecutionContext, Future}

trait ConnectionService {
  this: AbstractConfigurationService =>

  private val connectionPool: MongoConnection = ConnectionService.driver.connection(dbHost :: Nil)

  //Is that correct? TODO

  protected def db() (implicit ec : ExecutionContext): Future[DefaultDB] = connectionPool.database(dbDb)

}

object ConnectionService {
  val driver = new MongoDriver()
}
