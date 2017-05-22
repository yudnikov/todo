package ru.yudnikov.todo.models

import java.util.UUID

import ru.yudnikov.database.cassandra.Indexed
import ru.yudnikov.engine.{Manager, Model}
import ru.yudnikov.todo.ui.bridge.LoginRequest
import ru.yudnikov.todo.ui.views.LoginView

import scala.annotation.meta.field
import ru.yudnikov.todo.Config._
/**
  * Created by Don on 19.04.2017.
  */
class UserModel(
                 @(Indexed @field)
                 val name: String,
                 val password: String,
                 val id: UUID = UUID.randomUUID()
               ) extends Model with Serializable {

}

object User extends Manager {

  def authenticate(loginRequest: LoginRequest): Option[UserModel] = {
    super.load(classOf[UserModel], loginRequest.name, "name") match {
      case Some(userModel) if userModel.password == loginRequest.password =>
        Some(userModel)
      case _ => None
    }
  }

  def create(userModel: UserModel): Unit = {

  }

}
