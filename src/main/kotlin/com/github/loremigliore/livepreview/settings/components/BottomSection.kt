package com.github.loremigliore.livepreview.settings.components

import com.github.loremigliore.livepreview.LivePreviewBundle
import com.github.loremigliore.livepreview.settings.LivePreviewSettings
import com.github.loremigliore.livepreview.utils.requireProject
import com.intellij.icons.AllIcons
import com.intellij.openapi.ui.Messages
import com.intellij.util.ui.JBUI
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingConstants

class BottomSection(
    private val customizationSection: CustomizationSection,
    private val mainFunctionSection: MainFunctionSection,
) {
    val resetButton =
        JButton(LivePreviewBundle.message("settings.reset.button.text"), AllIcons.General.Reset).apply {

            margin = JBUI.insets(4, 10)
            isFocusable = false
            horizontalTextPosition = SwingConstants.LEFT

            addActionListener {
                val confirm =
                    Messages.showYesNoDialog(
                        requireProject(),
                        LivePreviewBundle.message("settings.reset.dialog.message"),
                        LivePreviewBundle.message("settings.reset.dialog.title"),
                        null,
                    )

                if (confirm == Messages.YES) {
                    val settings = LivePreviewSettings.getInstance()
                    settings.reset()
                    customizationSection.loadFrom(settings)
                    mainFunctionSection.loadFrom(settings)
                }
            }
        }

    val component =
        object : JPanel() {
            init {
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                border = JBUI.Borders.empty(16, 24, 0, 24)
                add(resetButton)
                add(Box.createHorizontalGlue())
            }
        }

    fun isModified(settings: LivePreviewSettings): Boolean =
        customizationSection.isModified(settings) || mainFunctionSection.isModified(settings)

    fun applyTo(settings: LivePreviewSettings) {
        customizationSection.isModified(settings)
        mainFunctionSection.applyTo(settings)
    }
}
