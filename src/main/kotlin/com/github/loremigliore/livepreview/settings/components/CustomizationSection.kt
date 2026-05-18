package com.github.loremigliore.livepreview.settings.components

import com.github.loremigliore.livepreview.LivePreviewBundle
import com.github.loremigliore.livepreview.settings.LivePreviewSettings
import com.github.loremigliore.livepreview.theme.LivePreviewTheme
import com.intellij.util.ui.JBUI
import java.awt.Dimension
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.SwingConstants

class CustomizationSection {
    private fun settings() = LivePreviewSettings.getInstance()

    private val windowWidthLabel =
        JLabel(LivePreviewBundle.message("settings.window.width.label"), SwingConstants.LEFT)

    private val windowWidthTextField =
        JTextField(5).apply {
            text = settings().windowWidth.toString()
            font = LivePreviewTheme.mainFont
            horizontalAlignment = SwingConstants.RIGHT
            maximumSize = Dimension(100, 40)
            columns = 5
        }

    private val windowWidthJPanel =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(windowWidthLabel)
            add(Box.createHorizontalStrut(12))
            add(windowWidthTextField)
        }

    private val windowHeightLabel =
        JLabel(LivePreviewBundle.message("settings.window.height.label"), SwingConstants.LEFT)

    private val windowHeightTextField =
        JTextField(5).apply {
            text = settings().windowHeight.toString()
            font = LivePreviewTheme.mainFont
            horizontalAlignment = SwingConstants.RIGHT
            maximumSize = Dimension(100, 40)
            columns = 5
        }
    private val windowHeightJPanel =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(windowHeightLabel)
            add(Box.createHorizontalStrut(12))
            add(windowHeightTextField)
        }

    private val textfieldGroup =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = JBUI.Borders.empty(0, 16)
            add(windowWidthJPanel)
            add(Box.createVerticalStrut(8))
            add(windowHeightJPanel)
        }

    private val alwaysOnTopCheckbox =
        JCheckBox(LivePreviewBundle.message("settings.always.on.top.label")).apply {
            isSelected = settings().alwaysOnTop
        }

    private val autoGenerationCheckbox =
        JCheckBox(LivePreviewBundle.message("settings.auto.generation.label")).apply {
            isSelected = settings().autoGeneration
        }

    private val checkboxGroup =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = JBUI.Borders.empty(0, 16)
            add(alwaysOnTopCheckbox)
            add(Box.createVerticalStrut(8))
            add(autoGenerationCheckbox)
        }

    val component =
        object : JPanel() {
            init {
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                border = JBUI.Borders.empty(4, 16, 16, 16)

                add(Box.createHorizontalGlue())
                add(textfieldGroup)
                add(Box.createHorizontalStrut(16))
                add(checkboxGroup)
                add(Box.createHorizontalGlue())
            }

            override fun getMaximumSize(): Dimension {
                val pref = preferredSize
                return Dimension(Int.MAX_VALUE, pref.height)
            }
        }

    fun loadFrom(settings: LivePreviewSettings) {
        windowWidthTextField.text = settings.windowWidth.toString()
        windowHeightTextField.text = settings.windowHeight.toString()
        alwaysOnTopCheckbox.isSelected = settings.alwaysOnTop
        autoGenerationCheckbox.isSelected = settings.autoGeneration
    }

    fun applyTo(settings: LivePreviewSettings) {
        settings.windowWidth = windowWidthTextField.text.toIntOrNull() ?: LivePreviewSettings.DEFAULT_WINDOW_WIDTH
        settings.windowHeight = windowHeightTextField.text.toIntOrNull() ?: LivePreviewSettings.DEFAULT_WINDOW_HEIGHT
        settings.alwaysOnTop = alwaysOnTopCheckbox.isSelected
        settings.autoGeneration = autoGenerationCheckbox.isSelected
    }

    fun isModified(settings: LivePreviewSettings): Boolean =
        windowWidthTextField.text != settings.windowWidth.toString() ||
            windowHeightTextField.text != settings.windowHeight.toString() ||
            alwaysOnTopCheckbox.isSelected != settings.alwaysOnTop ||
            autoGenerationCheckbox.isSelected != settings.autoGeneration
}
