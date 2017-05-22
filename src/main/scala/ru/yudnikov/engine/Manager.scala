package ru.yudnikov.engine

import java.util.UUID

import ru.yudnikov.database.cassandra.Cassandra
import ru.yudnikov.meta.Reflector

/**
  * Created by Don on 19.04.2017.
  */
trait Manager {

  private var models: Map[UUID, Model] = Map()

  def get[T <: Model](id: UUID): Option[T] = {
    models.get(id) match {
      case Some(model) => Some(model.asInstanceOf[T])
      case None => None
    }
  }

  def get[T <: Model](ref: Reference[T]): Option[T] = {
    models.get(ref.id) match {
      case Some(model) => Some(model.asInstanceOf[T])
      case None =>
        load(ref.aClass, ref.id) match {
          case Some(model) =>
            update(model)
            Some(model)
          case None => None
        }
    }
  }

  def update(model: Model): Unit = {
    models = models + (model.id -> model)
  }

  def save(model: Model): Unit = {
    println(s"[Manager]: save($model)")
    Cassandra.save(model)
  }

  def load[T <: Model](aClass: Class[T], id: UUID): Option[T] = {
    load(aClass, id, "id")
  }

  def load[T <: Model](aClass: Class[T], propertyValue: Any, propertyName: String = "id"): Option[T] = {
    Reflector.describe(aClass, propertyName, propertyValue) match {
      case Some(propertyDescription) => Cassandra.load(propertyDescription)
      case _ => None
    }
  }

  def list[T <: Model](aClass: Class[T], propertyValue: Any, propertyName: String): List[T] = {
    Reflector.describe(aClass, propertyName, propertyValue) match {
      case Some(propertyDescription) => Cassandra.list(propertyDescription)
      case _ => List[T]()
    }
  }

  def list[T <: Model](aClass: Class[T]): List[T] = {
    Cassandra.list(aClass)
  }

}

object Manager extends Manager
