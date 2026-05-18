package com.github.loremigliore.livepreview.settings

import com.github.loremigliore.livepreview.theme.LivePreviewTheme
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "com.loremigliore.livepreview.logic.services.settings.LivePreviewSettings",
    storages = [Storage("livepreview_settings.xml")],
)
@Service
class LivePreviewSettings : PersistentStateComponent<LivePreviewSettings> {
    companion object {
        const val DEFAULT_WINDOW_WIDTH: Int = LivePreviewTheme.DEFAULT_WINDOW_WIDTH
        const val DEFAULT_WINDOW_HEIGHT: Int = LivePreviewTheme.DEFAULT_WINDOW_HEIGHT
        private const val DEFAULT_ALWAYS_ON_TOP: Boolean = true
        private const val DEFAULT_AUTO_GENERATION: Boolean = true
        private const val DEFAULT_MAIN_FUNTION: String =
            "fun main() = application {\n" +
                "    Window(onCloseRequest = ::exitApplication,\n" +
                "           title = \"Hot Reload Preview: <COMPOSABLE_NAME>\",\n" +
                "           alwaysOnTop = <ALWAYS_ON_TOP>,\n" +
                "           state = WindowState(width = <WIDTH>.dp, height = <HEIGHT>.dp)) {\n" +
                "        <COMPOSABLE>\n" +
                "    }\n" +
                "}"

        fun getInstance(): LivePreviewSettings = ApplicationManager.getApplication().getService(LivePreviewSettings::class.java)
    }

    var windowWidth = DEFAULT_WINDOW_WIDTH
    var windowHeight = DEFAULT_WINDOW_HEIGHT
    var alwaysOnTop = DEFAULT_ALWAYS_ON_TOP
    var autoGeneration = DEFAULT_AUTO_GENERATION
    var mainFunction = DEFAULT_MAIN_FUNTION

    override fun getState(): LivePreviewSettings = this

    override fun loadState(state: LivePreviewSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    fun reset() {
        windowWidth = DEFAULT_WINDOW_WIDTH
        windowHeight = DEFAULT_WINDOW_HEIGHT
        alwaysOnTop = DEFAULT_ALWAYS_ON_TOP
        autoGeneration = DEFAULT_AUTO_GENERATION
        mainFunction = DEFAULT_MAIN_FUNTION
    }
}
