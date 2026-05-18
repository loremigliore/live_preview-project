package com.github.loremigliore.livepreview.toolWindow.components
import com.github.loremigliore.livepreview.domain.ComposableData
import com.github.loremigliore.livepreview.utils.livePreviewStateHolder
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingUtilities

class WindowContent(
    val project: Project,
) {
    private val windowListContent =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = JBUI.Borders.empty(0, 10, 10, 10)
        }

    val component: JComponent =
        JBScrollPane(windowListContent).apply {
            horizontalScrollBarPolicy = JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            verticalScrollBarPolicy = JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            border = JBUI.Borders.empty()

            viewport.background = JBColor.background()
        }

    init {
        project.livePreviewStateHolder.state
            .onEach { state ->
                SwingUtilities.invokeLater {
                    updateComposables(state.composables)
                }
            }.launchIn(project.livePreviewStateHolder.cs)
    }

    private fun updateComposables(composables: List<ComposableData>) {
        windowListContent.removeAll()

        if (composables.isEmpty()) {
            windowListContent.add(Box.createVerticalGlue())
            windowListContent.add(EmptyLabel.component)
            windowListContent.add(Box.createVerticalGlue())
        } else {
            windowListContent.add(ComposableListHeader(composables.first()).component)

            composables.forEach { composable ->
                val item = ComposableListItem(project, composable).component
                windowListContent.add(item)
                windowListContent.add(Box.createVerticalStrut(12))
            }
            windowListContent.add(Box.createVerticalGlue())
        }

        windowListContent.revalidate()
        windowListContent.repaint()
    }
}
