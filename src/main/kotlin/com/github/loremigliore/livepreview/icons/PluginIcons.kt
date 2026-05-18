package com.github.loremigliore.livepreview.icons

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object PluginIcons {
    @JvmField
    val ToolWindowIcon: Icon = IconLoader.getIcon("/icons/toolWindow.svg", javaClass)

    @JvmField
    val RunPreview: Icon = IconLoader.getIcon("/icons/play.svg", javaClass)
}
