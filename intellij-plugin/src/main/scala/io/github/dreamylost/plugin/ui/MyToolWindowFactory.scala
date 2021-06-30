// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package io.github.dreamylost.plugin.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.{ ToolWindow, ToolWindowFactory }
import com.intellij.ui.components.JBLabel
import io.github.dreamylost.plugin.PluginBundle

class MyToolWindowFactory extends ToolWindowFactory {

  override def createToolWindowContent(project: Project, toolWindow: ToolWindow): Unit = {
    toolWindow.getComponent.add(new JBLabel(PluginBundle.message("my.cool.tool.window")))
  }

}
