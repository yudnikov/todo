package ru.yudnikov.todo.ui.layouts

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.navigator.{Navigator, ViewChangeListener}
import com.vaadin.server.Page
import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import com.vaadin.ui._
import ru.yudnikov.todo.models.{TodoModel, UserModel}
import ru.yudnikov.todo.ui.TodoUI
import ru.yudnikov.todo.ui.designs.Root
import ru.yudnikov.todo.ui.views.{LoginView, TodoEditView, TodoListView}

/**
  * Created by Don on 19.04.2017.
  */
class RootLayout(todoUI: TodoUI) extends Root {

  setExpandRatio(contentPanel, 1)

  def onLogin(): Unit = {
    setMainLogoText()
    buttons.setVisible(true)
  }

  def onLogout(): Unit = {
    setMainLogoText()
    buttons.setVisible(false)
  }

  def setMainLogoText(): Unit = {
    val welcome = todoUI.getSession.getAttribute(classOf[UserModel]) match {
      case userModel: UserModel => s"Hello, ${userModel.name}! You have to do:"
      case _ => ""
    }
    mainLogo.setValue(welcome)
  }

  logoutButton.addClickListener(new ClickListener {
    override def buttonClick(clickEvent: ClickEvent) = {
      getSession.setAttribute(classOf[UserModel], null)
      todoUI.removeCookie("user")
      todoUI.getSession.close()
      Page.getCurrent.reload()
    }
  })

  settingsButton.addClickListener(new ClickListener {
    override def buttonClick(clickEvent: ClickEvent) = {
      val settingsWindow = new Window("Settings")
      settingsWindow.setContent(new SettingsLayout(settingsWindow))
      settingsWindow.setResizable(false)
      settingsWindow.center()
      getUI.addWindow(settingsWindow)
    }
  })

  addTodoButton.addClickListener(new ClickListener {
    override def buttonClick(event: ClickEvent) = {
      getUI.getNavigator.navigateTo(s"${classOf[TodoEditView].getSimpleName}/${new TodoModel().id}")
    }
  })

  val navigator = new Navigator(todoUI, contentLayout)
  navigator.addView("", classOf[TodoListView])
  navigator.addView(classOf[LoginView].getSimpleName, classOf[LoginView])
  navigator.addView(classOf[TodoEditView].getSimpleName, new TodoEditView(todoUI))

  navigator.addViewChangeListener(new ViewChangeListener {
    override def afterViewChange(viewChangeEvent: ViewChangeEvent) = {

    }
    override def beforeViewChange(viewChangeEvent: ViewChangeEvent) = {
      val userModel = getSession.getAttribute(classOf[UserModel])
      if (viewChangeEvent.getNewView.getClass != classOf[LoginView] && userModel == null) {
        println(s"[RootLayout]: access denied!")
        getUI.getNavigator.navigateTo(classOf[LoginView].getSimpleName)
        false
      }
      else if (viewChangeEvent.getNewView.getClass == classOf[LoginView] && userModel != null) {
        println(s"[RootLayout]: access denied!")
        getUI.getNavigator.navigateTo("")
        false
      }
      else
        true
    }
  })

}
