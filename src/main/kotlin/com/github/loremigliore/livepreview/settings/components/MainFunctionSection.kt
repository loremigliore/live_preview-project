package com.github.loremigliore.livepreview.settings.components

import com.github.loremigliore.livepreview.LivePreviewBundle
import com.github.loremigliore.livepreview.settings.LivePreviewSettings
import com.github.loremigliore.livepreview.theme.LivePreviewTheme
import com.intellij.openapi.project.ProjectManager
import com.intellij.ui.LanguageTextField
import com.intellij.util.ui.JBUI
import org.jetbrains.kotlin.idea.KotlinLanguage
import java.awt.Dimension
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel

class MainFunctionSection {
    val project = ProjectManager.getInstance().defaultProject

    val mainFunctionTextField =
        object : LanguageTextField(
            KotlinLanguage.INSTANCE,
            project,
            LivePreviewSettings.getInstance().mainFunction,
            false,
        ) {
            override fun createEditor(): com.intellij.openapi.editor.ex.EditorEx {
                val editor = super.createEditor()
                editor.settings.apply {
                    isUseSoftWraps = true
                    isLineNumbersShown = false
                    isFoldingOutlineShown = false
                    isWhitespacesShown = false
                }
                editor.setVerticalScrollbarVisible(true)
                editor.setHorizontalScrollbarVisible(false)
                return editor
            }
        }.apply {
            font = LivePreviewTheme.mainFont
            toolTipText = LivePreviewBundle.message("settings.custom.mainFunction.textfield.tooltip")
            preferredSize = Dimension(400, 150)
        }

    val component =
        object : JPanel() {
            init {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                border = JBUI.Borders.empty(4, 16)
                add(mainFunctionTextField)
                add(Box.createVerticalStrut(8))

                add(
                    JLabel(
                        "-" + LivePreviewBundle.message("settings.custom.main.function.description2") +
                            ": <COMPOSABLE>",
                    ).apply {
                        border =
                            JBUI.Borders.emptyLeft(16)
                    },
                )

                add(Box.createVerticalStrut(4))

                add(
                    JLabel(
                        "-" +
                            LivePreviewBundle.message(
                                "settings.custom.main.function.description3",
                            ) + ": <COMPOSABLE_NAME>, <WIDTH>, <HEIGHT>, <ALWAYS_ON_TOP>",
                    ).apply {
                        border =
                            JBUI.Borders.emptyLeft(16)
                    },
                )
            }

            override fun getMaximumSize(): Dimension {
                val pref = preferredSize
                return Dimension(Int.MAX_VALUE, pref.height)
            }
        }

    fun loadFrom(settings: LivePreviewSettings) {
        mainFunctionTextField.text = settings.mainFunction
    }

    fun applyTo(settings: LivePreviewSettings) {
        settings.mainFunction = mainFunctionTextField.text
    }

    fun isModified(settings: LivePreviewSettings): Boolean = mainFunctionTextField.text != settings.mainFunction
}
