package ru.yudnikov.todo.ui.layouts

import com.vaadin.server.Page
import com.vaadin.ui.Button.{ClickEvent, ClickListener}
import com.vaadin.ui.{Button, Window}
import ru.yudnikov.todo.ui.designs.Settings

/**
  * Created by Don on 20.04.2017.
  */
class SettingsLayout(window: Window) extends Settings {

  saveButton.addClickListener(new ClickListener {
    override def buttonClick(clickEvent: ClickEvent) = {
      getSession.setAttribute("isCompletedVisible", isCompletedVisible.getValue)
      window.close()
      Page.getCurrent.reload()
    }
  })

}
