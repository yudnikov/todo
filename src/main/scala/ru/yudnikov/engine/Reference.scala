package ru.yudnikov.engine

import java.util.UUID

/**
  * Created by Don on 14.04.2017.
  */
class Reference[T <: Model](val aClass: Class[T], val id: UUID) {

  def get: Option[T] = Manager.get(this)

  override def toString: String = {
    id.toString
  }

}
