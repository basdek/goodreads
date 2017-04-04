package com.basdek.goodreads

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer


object Server extends App {

  implicit val system = ActorSystem("goodreads")
  implicit val materializer = ActorMaterializer()

  implicit val executionContext = system.dispatcher


}
