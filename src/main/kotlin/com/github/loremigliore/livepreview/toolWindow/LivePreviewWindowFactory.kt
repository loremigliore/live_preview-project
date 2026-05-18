package com.github.loremigliore.livepreview.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class LivePreviewWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow,
    ) {
        val toolWindowContent = LivePreviewToolWindowContent(project)
        val content = ContentFactory.getInstance().createContent(toolWindowContent.windowPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
