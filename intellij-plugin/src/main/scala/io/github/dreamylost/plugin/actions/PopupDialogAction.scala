// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package io.github.dreamylost.plugin.actions

import com.intellij.openapi.actionSystem.{ AnAction, AnActionEvent, CommonDataKeys }
import com.intellij.openapi.ui.Messages
import io.github.dreamylost.plugin.PluginBundle
import io.github.dreamylost.plugin.services.{ ApplicationHelloService, ProjectHelloService }

class PopupDialogAction extends AnAction() {

  /**
   * Gives the user feedback when the dynamic action menu is chosen.
   * Pops a simple message dialog.
   * @param event Event received when the associated menu item is chosen.
   */
  override def actionPerformed(event: AnActionEvent): Unit = { // Using the event, create and show a dialog
    val currentProject = event.getProject
    val dlgMsg = new StringBuilder(PluginBundle.message("gettext.selected", event.getPresentation.getText) + '\n')
    val dlgTitle = event.getPresentation.getDescription

    // If an element is selected in the editor, add info about it.
    val nav = event.getData(CommonDataKeys.NAVIGATABLE)
    if (nav != null)
      dlgMsg.append(PluginBundle.message("selected.element.tostring", nav.toString) + '\n')

    val appHelloMessage = ApplicationHelloService.getInstance.getApplicationHelloInfo
    dlgMsg.append(appHelloMessage + '\n')

    val projectMessage = ProjectHelloService.getInstance(currentProject).getProjectHelloInfo
    dlgMsg.append(projectMessage + '\n')

    Messages.showMessageDialog(currentProject, dlgMsg.toString, dlgTitle, Messages.getInformationIcon)
  }

  /**
   * Determines whether this menu item is available for the current context.
   * Requires a project to be open.
   *
   * @param e Event received when the associated group-id menu is chosen.
   */
  override def update(e: AnActionEvent): Unit = { // Set the availability based on whether a project is open
    val project = e.getProject
    e.getPresentation.setEnabledAndVisible(project != null)
  }
}
