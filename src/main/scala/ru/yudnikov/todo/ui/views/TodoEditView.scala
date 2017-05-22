package ru.yudnikov.todo.ui.views

import java.util
import java.util.UUID
import javafx.collections.FXCollections

import com.vaadin.data.util.ObjectProperty
import com.vaadin.data.{Container, Property}
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import com.vaadin.ui.ComboBox
import org.joda.time.DateTime
import ru.yudnikov.engine.Manager
import ru.yudnikov.todo.models.{Todo, TodoModel, UserModel}
import ru.yudnikov.todo.ui.TodoUI
import ru.yudnikov.todo.ui.designs.TodoEdit

import collection.JavaConverters._

/**
  * Created by Don on 24.04.2017.
  */
class TodoEditView(todoUI: TodoUI) extends TodoEdit with View {

  //addComponent()

  def fillUserComboBox(comboBox: ComboBox, currentUserModel: UserModel, isList: Boolean = true): ComboBox = {
    comboBox.removeAllItems()
    comboBox.setNullSelectionAllowed(false)
    val list = if (isList) Manager.list(classOf[UserModel]) else List(currentUserModel)
    list.foreach(userModel => {
      comboBox.addItem(userModel.id)
      comboBox.setItemCaption(userModel.id, userModel.name)
      comboBox.select(currentUserModel.id)
    })
    comboBox
  }

  override def enter(event: ViewChangeEvent): Unit = {

    getSession.getAttribute(classOf[UserModel]) match {
      case userModel: UserModel =>
        executorComboBox = fillUserComboBox(executorComboBox, userModel)
        customerComboBox = fillUserComboBox(customerComboBox, userModel, false)
    }

    val todoModel = event.getParameters.split("/") match {
      case a: Array[String] if a.length == 1 =>
        Todo.get(UUID.fromString(a(0))).get.asInstanceOf[TodoModel]
      case _ => new TodoModel()
    }

    todoArea.setValue(todoModel.todo)
    dateField.setValue(todoModel.date.toDate)

    saveButton.addClickListener(new ClickListener {
      override def buttonClick(event: ClickEvent) = {
        val executor = Manager.get(executorComboBox.getValue.asInstanceOf[UUID]).get.asInstanceOf[UserModel]
        val customer = Manager.get(customerComboBox.getValue.asInstanceOf[UUID]).get.asInstanceOf[UserModel]
        todoModel.withDate(new DateTime(dateField.getValue)).withTodo(todoArea.getValue).withCustomer(customer).withExecutor(executor).save()
        getUI.getNavigator.navigateTo("")
      }
    })
  }
}
