package ru.yudnikov.todo.ui.views

import java.util.Date

import com.vaadin.data.Property.{ValueChangeEvent, ValueChangeListener}
import com.vaadin.event.ContextClickEvent
import com.vaadin.event.ContextClickEvent.ContextClickListener
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import com.vaadin.ui.{Notification, PopupView}
import org.joda.time.DateTime
import ru.yudnikov.todo.models.TodoModel
import ru.yudnikov.todo.ui.designs.Todo
import ru.yudnikov.todo.ui.layouts.TodoContextLayout

/**
  * Created by Don on 19.04.2017.
  */
class TodoView(todoModel: TodoModel, val parent: TodoListView, val isExecutable: Boolean = true) extends Todo with View {

  todo.setValue(todoModel.todo)
  todo.addContextClickListener(new ContextClickListener {
    override def contextClick(event: ContextClickEvent) = {
      //val p = new PopupView("To do with todo: ", new TodoContextLayout)
      //Notification.show(s"x=${event.getClientX}, y=${event.getClientY}")
      //parent.addComponent(p)
    }
  })

  val p = new PopupView("***", new TodoContextLayout)
  root.addComponent(p)

  /*
  contextButton.addClickListener(new ClickListener {
    override def buttonClick(event: ClickEvent) = {
      val p = new PopupView("To do with todo: ", new TodoContextLayout)
    }
  })
  */

  isComplete.setValue(todoModel.isCompleted)
  isComplete.setVisible(isExecutable)
  dateField.setValue(todoModel.date.toDate)
  val v = if (isExecutable)
    s"from: ${todoModel.customer.get.get.name}"
  else
    s"to: ${todoModel.executor.get.get.name}"
  directionLabel.setValue(v)
  //directionLabel.setVisible(isExecutable)

  setVisible(!todoModel.isCompleted | parent.isCompletedVisible)

  isComplete.addValueChangeListener(new ValueChangeListener {
    override def valueChange(valueChangeEvent: ValueChangeEvent) = {
      val value = valueChangeEvent.getProperty.getValue.asInstanceOf[Boolean]
      Thread.sleep(100)
      todoModel.withIsCompleted(valueChangeEvent.getProperty.getValue.asInstanceOf[Boolean]).save()
      setVisible(!value | parent.isCompletedVisible)
    }
  })

  dateField.addValueChangeListener(new ValueChangeListener {
    override def valueChange(event: ValueChangeEvent) = {
      val value = new DateTime(event.getProperty.getValue.asInstanceOf[Date])
      todoModel.withDate(value).save()
    }
  })

  override def enter(viewChangeEvent: ViewChangeEvent): Unit = {

  }

}
