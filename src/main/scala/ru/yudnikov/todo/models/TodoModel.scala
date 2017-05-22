package ru.yudnikov.todo.models

import java.util.{Date, UUID}

import org.joda.time.DateTime
import ru.yudnikov.database.cassandra.Indexed
import ru.yudnikov.engine.{Manager, Model, Reference}

import scala.annotation.meta.field
import ru.yudnikov.todo.Config._

/**
  * Created by Don on 19.04.2017.
  */
class TodoModel(
                 val todo: String,
                 @(Indexed @field)
                 val executor: Reference[UserModel],
                 @(Indexed @field)
                 val customer: Reference[UserModel],
                 val date: DateTime,
                 val isCompleted: Boolean = false,
                 val id: UUID = UUID.randomUUID()
               ) extends Model {

  def this() {
    this("To do something important...", null, null, new DateTime())
  }

  override def update(): Unit = {
    Todo.update(this)
  }

  def withIsCompleted(isCompleted: Boolean): TodoModel = {
    new TodoModel(todo, executor, customer, date, isCompleted, id)
  }

  def withDate(dateTime: DateTime): TodoModel = {
    new TodoModel(todo, executor, customer, dateTime, isCompleted, id)
  }

  def withTodo(todo: String): TodoModel = {
    new TodoModel(todo, executor, customer, date, isCompleted, id)
  }

  def withCustomer(customer: UserModel): TodoModel = {
    new TodoModel(todo, executor, customer.reference, date, isCompleted, id)
  }

  def withExecutor(executor: UserModel): TodoModel = {
    new TodoModel(todo, executor.reference, customer, date, isCompleted, id)
  }

}

object Todo extends Manager {

  override def update(model: Model): Unit = {
    super.update(model)
  }

}