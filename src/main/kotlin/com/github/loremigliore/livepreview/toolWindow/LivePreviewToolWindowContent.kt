package com.github.loremigliore.livepreview.toolWindow

import com.github.loremigliore.livepreview.toolWindow.components.WindowContent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel

class LivePreviewToolWindowContent(
    project: Project,
) {
    private val windowContent = WindowContent(project)

    val windowPanel =
        SimpleToolWindowPanel(true, true).apply {
            setContent(windowContent.component)
        }
}
