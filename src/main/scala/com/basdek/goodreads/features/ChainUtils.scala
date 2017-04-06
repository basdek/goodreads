package com.basdek.goodreads.features

import scala.concurrent.{ExecutionContext, Future}
import scalaz.\/
import scalaz.Scalaz._

object ChainUtils {

  def predicateInjector[T](future : Future[T], predicate : Function[T, Boolean], error: Error)
    (implicit ec: ExecutionContext) : Future[Error \/ T] = {
    future map { res =>
      if (predicate(res)) res.right else error.left
    }
  }

  def lift[T](f : Future[T]) (implicit ec: ExecutionContext) : Future[Error \/ T] = {
    f map {res => res.right}
  }
}
