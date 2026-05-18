package com.github.loremigliore.livepreview.toolWindow.components

import com.github.loremigliore.livepreview.LivePreviewBundle
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import java.awt.Component
import javax.swing.SwingConstants

object EmptyLabel {
    val component =
        JBLabel(LivePreviewBundle.message("toolwindow.empty.list.message")).apply {
            horizontalAlignment = SwingConstants.CENTER
            alignmentX = Component.CENTER_ALIGNMENT
            foreground = JBColor.GRAY
        }
}
