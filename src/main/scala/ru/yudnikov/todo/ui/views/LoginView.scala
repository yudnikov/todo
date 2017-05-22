package ru.yudnikov.todo.ui.views

import javax.servlet.http.Cookie

import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.server.{Page, VaadinService}
import com.vaadin.ui.Button
import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import ru.yudnikov.todo.models.{User, UserModel}
import ru.yudnikov.todo.ui.TodoUI
import ru.yudnikov.todo.ui.bridge.LoginRequest
import ru.yudnikov.todo.ui.designs.Login



/**
  * Created by Don on 19.04.2017.
  */
class LoginView extends Login with View {

  loginButton.addClickListener(new ClickListener {
    override def buttonClick(clickEvent: ClickEvent) = {

      val loginRequest = LoginRequest(
        loginField.getValue,
        passwordField.getValue,
        rememberMe.getValue
      )

      User.authenticate(loginRequest) match {
        case Some(userModel) =>
          val todoUI = getUI.asInstanceOf[TodoUI]
          getSession.setAttribute(classOf[UserModel], userModel)
          todoUI.setCookie("user", userModel.id.toString)
          todoUI.onLogin()
          getUI.getNavigator.navigateTo("")
          warningLabel.setVisible(false)
        case _ =>
          warningLabel.setVisible(true)
      }

    }
  })

  override def enter(viewChangeEvent: ViewChangeEvent): Unit = {

  }

}

