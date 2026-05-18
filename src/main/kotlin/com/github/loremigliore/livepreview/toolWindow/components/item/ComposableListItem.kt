package com.github.loremigliore.livepreview.toolWindow.components

import com.github.loremigliore.livepreview.domain.ComposableData
import com.github.loremigliore.livepreview.theme.LivePreviewTheme
import com.github.loremigliore.livepreview.toolWindow.components.item.ComposableListItemHeader
import com.github.loremigliore.livepreview.utils.PreviewFileManager
import com.github.loremigliore.livepreview.utils.livePreviewStateHolder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JPanel
import javax.swing.event.DocumentEvent
import kotlin.time.Duration.Companion.milliseconds

class ComposableListItem(
    project: Project,
    composable: ComposableData,
) {
    val component: JPanel =
        object : JPanel(BorderLayout(0, 8)) {
            init {
                border =
                    JBUI.Borders.compound(
                        JBUI.Borders.customLine(JBColor.border(), 1),
                        JBUI.Borders.empty(10),
                    )
                alignmentX = CENTER_ALIGNMENT

                val headerPanel = ComposableListItemHeader(project, composable).component

                val codeTextArea =
                    JBTextArea(composable.code).apply {
                        font = LivePreviewTheme.codeFont
                        lineWrap = true
                        wrapStyleWord = true
                        border =
                            JBUI.Borders.compound(
                                JBUI.Borders.customLine(JBColor.border(), 1),
                                JBUI.Borders.empty(4),
                            )
                    }

                var debounceJob: Job? = null

                codeTextArea.document.addDocumentListener(
                    object : DocumentAdapter() {
                        override fun textChanged(e: DocumentEvent) {
                            revalidate()
                            val updatedText = codeTextArea.text

                            debounceJob?.cancel()
                            debounceJob =
                                project.livePreviewStateHolder.cs.launch {
                                    delay(LivePreviewTheme.DEBOUNCE_DURATION.milliseconds)

                                    ApplicationManager.getApplication().executeOnPooledThread {
                                        PreviewFileManager.writePreviewCode(
                                            project = project,
                                            fqName = composable.fqName,
                                            code = updatedText,
                                        )
                                    }
                                }
                        }
                    },
                )

                add(headerPanel, BorderLayout.NORTH)
                add(codeTextArea, BorderLayout.CENTER)
            }

            override fun getMaximumSize() = Dimension(Int.MAX_VALUE, preferredSize.height)
        }
}
