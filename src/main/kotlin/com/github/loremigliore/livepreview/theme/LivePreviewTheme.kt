package com.github.loremigliore.livepreview.theme

import com.intellij.util.ui.JBUI
import java.awt.Font

object LivePreviewTheme {
    val mainFont: Font get() = JBUI.Fonts.create("JetBrains Mono", 14)
    val codeFont: Font get() = JBUI.Fonts.create("JetBrains Mono", 12)

    const val DEBOUNCE_DURATION = 500

    const val DEFAULT_WINDOW_WIDTH = 720
    const val DEFAULT_WINDOW_HEIGHT = 576
}
