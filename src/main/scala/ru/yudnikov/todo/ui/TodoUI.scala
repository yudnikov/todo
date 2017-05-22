package ru.yudnikov.todo.ui

import java.util.UUID
import javax.servlet.http.Cookie

import com.vaadin.annotations.Theme
import com.vaadin.server.{VaadinRequest, VaadinService}
import com.vaadin.ui._
import ru.yudnikov.engine.Manager
import ru.yudnikov.todo.models.UserModel
import ru.yudnikov.todo.ui.layouts.RootLayout

/**
  * Created by Don on 19.04.2017.
  */
@Theme("mytheme")
class TodoUI extends UI {

  private var windows: Set[Window] = Set()

  override def addWindow(window: Window): Unit = {
    windows = windows + window
    super.addWindow(window)
  }

  def getCookie(name: String, vaadinRequest: VaadinRequest = VaadinService.getCurrentRequest): Option[Cookie] = {
    try {
      vaadinRequest.getCookies.filter(c => c.getName == name) match {
        case cookies: Array[Cookie] if cookies.length == 1 => Some(cookies.head)
        case _ => None
      }
    } catch {
      case e: Exception =>
        println(e.getCause)
        None
    }
  }

  def removeCookie(name: String): Unit = {
    val cookie = new Cookie(name, null)
    cookie.setMaxAge(0)
    cookie.setPath("/")
    VaadinService.getCurrentResponse.addCookie(cookie)
  }

  def setCookie(name: String, value: String): Unit = {
    val cookie = new Cookie(name, value)
    cookie.setMaxAge(100500)
    cookie.setPath("/")
    VaadinService.getCurrentResponse.addCookie(cookie)
  }

  def onLogin(): Unit = {
    rootLayout.onLogin()
  }

  def onLogout(): Unit = {
    rootLayout.onLogout()
  }

  val rootLayout = new RootLayout(this)

  override def init(vaadinRequest: VaadinRequest): Unit = {
    getCookie("user") match {
      case Some(cookie) =>
        Manager.load(classOf[UserModel], UUID.fromString(cookie.getValue)) match {
          case Some(userModel) => getSession.setAttribute(classOf[UserModel], userModel)
          case None => removeCookie(cookie.getName)
        }
        rootLayout.onLogin()
      case _ =>
        rootLayout.onLogout()
    }
    setContent(rootLayout)
  }

}
