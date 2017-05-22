package ru.yudnikov.todo.ui.views

import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.ui.Layout
import ru.yudnikov.engine.Manager
import ru.yudnikov.todo.models.{TodoModel, UserModel}
import ru.yudnikov.todo.ui.TodoUI
import ru.yudnikov.todo.ui.designs.TodoList

/**
  * Created by Don on 19.04.2017.
  */
class TodoListView extends TodoList with View {

  var isCompletedVisible = false

  override def enter(viewChangeEvent: ViewChangeEvent): Unit = {

    println(s"[TodoListView]: enter")
    val userModel = getSession.getAttribute(classOf[UserModel])
    println(s"[TodoListView]: userModel = $userModel")

    isCompletedVisible = getSession.getAttribute("isCompletedVisible") match {
      case value: AnyRef if value.asInstanceOf[Boolean] => true
      case _ => false
    }

    val incoming = Manager.list(classOf[TodoModel], userModel.reference, "executor")
    incomingTodosLayout.addComponents(incoming.map(todoModel => new TodoView(todoModel, this)): _*)

    val outcoming = Manager.list(classOf[TodoModel], userModel.reference, "customer")
    outcomingTodosLayout.addComponents(outcoming.map(todoModel => new TodoView(todoModel, this, false)): _*)

  }
}
