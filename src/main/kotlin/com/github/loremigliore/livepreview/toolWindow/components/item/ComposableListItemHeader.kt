package com.github.loremigliore.livepreview.toolWindow.components.item

import com.github.loremigliore.livepreview.LivePreviewBundle
import com.github.loremigliore.livepreview.domain.ComposableData
import com.github.loremigliore.livepreview.icons.PluginIcons
import com.github.loremigliore.livepreview.logic.RunPreview
import com.github.loremigliore.livepreview.theme.LivePreviewTheme
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import java.awt.Component.LEFT_ALIGNMENT
import java.awt.Cursor
import java.awt.Dimension
import javax.swing.Box.createHorizontalStrut
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel

class ComposableListItemHeader(
    project: Project,
    composable: ComposableData,
) {
    val component: JPanel =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            isOpaque = false
            alignmentX = LEFT_ALIGNMENT

            val runButton =
                JButton(PluginIcons.RunPreview).apply {
                    isBorderPainted = false
                    isContentAreaFilled = false
                    isFocusPainted = false
                    isOpaque = false
                    margin = JBUI.emptyInsets()
                    border = null // Completely strip LookAndFeel padding/borders

                    // Clamp all Swing sizing hints to the exact icon bounds
                    val iconSize = Dimension(PluginIcons.RunPreview.iconWidth, PluginIcons.RunPreview.iconHeight)
                    preferredSize = iconSize
                    minimumSize = iconSize
                    maximumSize = iconSize

                    cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    toolTipText = LivePreviewBundle.message("run.button.tooltip")

                    addActionListener {
                        ApplicationManager.getApplication().invokeLater {
                            RunPreview.launchSafe(
                                project = project,
                                functionName = composable.name,
                                fqName = composable.fqName,
                                psiFile = null,
                            )
                        }
                    }
                }

            val nameLabel =
                JBLabel(composable.name).apply {
                    font = LivePreviewTheme.mainFont
                }

            add(createHorizontalStrut(8))
            add(runButton)
            add(createHorizontalStrut(8))
            add(nameLabel)
        }
}
