package com.github.loremigliore.livepreview.toolWindow.components

import com.github.loremigliore.livepreview.domain.ComposableData
import com.github.loremigliore.livepreview.theme.LivePreviewTheme
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JPanel
import javax.swing.SwingConstants

class ComposableListHeader(
    composable: ComposableData,
) {
    val component: JPanel =
        object : JPanel(BorderLayout()) {
            init {
                alignmentX = CENTER_ALIGNMENT
                isOpaque = false
                border = JBUI.Borders.empty(8)

                val fqNameLabel =
                    JBLabel(composable.fqName, SwingConstants.CENTER).apply {
                        font = LivePreviewTheme.mainFont
                    }

                add(fqNameLabel, BorderLayout.CENTER)
            }

            override fun getMaximumSize() = Dimension(Int.MAX_VALUE, preferredSize.height)
        }
}
