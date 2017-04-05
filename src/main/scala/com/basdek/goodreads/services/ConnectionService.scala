package com.basdek.goodreads.services

import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}

import scala.concurrent.Future

trait ConnectionService {
  this: AbstractConfigurationService =>

  private val driver = new MongoDriver()
  private val connectionPool: MongoConnection = driver.connection(dbHost :: Nil)

  //Is that correct? TODO
  implicit val executionContext = connectionPool.actorSystem.dispatcher

  protected def db(): Future[DefaultDB] = connectionPool.database(dbDb)

}
