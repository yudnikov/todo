package ru.yudnikov.todo

import javafx.collections.FXCollections

import org.joda.time.DateTime
import ru.yudnikov.database.cassandra.Cassandra
import ru.yudnikov.engine.Manager
import ru.yudnikov.todo.models.{TodoModel, User, UserModel}

/**
  * Created by Don on 22.04.2017.
  */
object TodoApp extends App {

  Cassandra.dropTable(classOf[UserModel])
  Cassandra.dropTable(classOf[TodoModel])

  val o = new UserModel("Oleg", "1234")
  o.save()
  val i = new UserModel("Igor", "1234")
  i.save()
  val m = new UserModel("Marina", "1234")
  m.save()

  val igor = Manager.load(classOf[UserModel], "Igor", "name").get
  val marina = Manager.load(classOf[UserModel], "Marina", "name").get

  var todo = new TodoModel("By coffee", igor.reference, marina.reference, new DateTime().plusDays(1))
  todo.save()

  todo = new TodoModel("Clean desktop", igor.reference, marina.reference, new DateTime().plusDays(7))
  todo.save()

  todo = new TodoModel("Cook porridge", marina.reference, igor.reference, new DateTime().plusDays(1))
  todo.save()

  val all = Manager.list(classOf[TodoModel])
  val igors = Manager.list(classOf[TodoModel], igor.reference, "executor")
  val marinas = Manager.list(classOf[TodoModel], marina.reference, "executor")
  null


  /*
  val a = User.authenticate(LoginRequest("Igor", "1234", true))
  val b = User.authenticate(LoginRequest("Oleg", "1234", true))

  println(a)
  println(b)
  null
  */

  /*
  val todos = Manager.list(classOf[TodoModel])
  val c = todos.head.customer
  null
  */

}
