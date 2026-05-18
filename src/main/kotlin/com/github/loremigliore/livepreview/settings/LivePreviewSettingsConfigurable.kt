package com.github.loremigliore.livepreview.settings

import com.github.loremigliore.livepreview.LivePreviewBundle
import com.github.loremigliore.livepreview.settings.components.BottomSection
import com.github.loremigliore.livepreview.settings.components.CustomizationSection
import com.github.loremigliore.livepreview.settings.components.MainFunctionSection
import com.intellij.openapi.options.Configurable
import com.intellij.ui.TitledSeparator
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.GroupLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

class LivePreviewSettingsConfigurable : Configurable {
    private val customizationSection = CustomizationSection()
    private val mainFunctionSection = MainFunctionSection()
    private val bottomSection = BottomSection(customizationSection, mainFunctionSection)

    override fun getDisplayName(): String = LivePreviewBundle.message("settings.display.name")

    override fun createComponent(): JComponent =
        JPanel(BorderLayout()).apply {
            val topPanel =
                JPanel().apply {
                    layout = BoxLayout(this, BoxLayout.Y_AXIS)

                    add(customizationSection.component)

                    add(
                        TitledSeparator(
                            LivePreviewBundle.message("settings.custom.main.function.description1"),
                        ).apply {
                            border = JBUI.Borders.emptyLeft(32)
                        },
                    )

                    add(Box.createVerticalStrut(8))
                }

            val functionPanel =
                JPanel(BorderLayout()).apply {
                    add(mainFunctionSection.component, BorderLayout.CENTER)
                }

            val bottomPanel =
                JPanel(BorderLayout()).apply {
                    add(bottomSection.component)
                }

            add(topPanel, BorderLayout.NORTH)
            add(functionPanel, BorderLayout.CENTER)
            add(bottomPanel, BorderLayout.SOUTH)
        }

    override fun reset() {
        val settings = LivePreviewSettings.getInstance()
        customizationSection.loadFrom(settings)
        mainFunctionSection.loadFrom(settings)
    }

    override fun isModified(): Boolean {
        val settings = LivePreviewSettings.getInstance()
        return customizationSection.isModified(settings) ||
            mainFunctionSection.isModified(settings) ||
            bottomSection.isModified(settings)
    }

    override fun apply() {
        val settings = LivePreviewSettings.getInstance()
        customizationSection.applyTo(settings)
        mainFunctionSection.applyTo(settings)
        bottomSection.applyTo(settings)
    }
}
