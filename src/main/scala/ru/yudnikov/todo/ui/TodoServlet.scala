package ru.yudnikov.todo.ui

import javax.servlet.annotation.WebServlet

import com.vaadin.annotations.VaadinServletConfiguration
import com.vaadin.server.VaadinServlet

/**
  * Created by Don on 19.04.2017.
  */
@WebServlet(urlPatterns =  Array("/*"), name = "TodoServlet", asyncSupported = true)
@VaadinServletConfiguration(ui = classOf[TodoUI], productionMode = false)
class TodoServlet extends VaadinServlet
