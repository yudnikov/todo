package ru.yudnikov.engine

import java.util.UUID

/**
  * Created by Don on 19.04.2017.
  */
abstract class Model(implicit val manager: Manager) {

  val id: UUID

  def update(): Unit = manager.update(this)

  def reference[T <: Model]: Reference[T] = new Reference[T](getClass.asInstanceOf[Class[T]], id)

  def save(): Unit = Manager.save(this)

  update()

}
