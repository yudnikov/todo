package ru.yudnikov.meta

import akka.actor.Actor
import ru.yudnikov.engine.Model

/**
  * Created by Don on 18.04.2017.
  */
class ReflectorActor extends Actor with Reflector {
  override def receive: Receive = {
    case model: Model =>
      sender() ! describe(model)
    case _ =>
      println(s"[ReflectorActor]: something received...")
  }
}


