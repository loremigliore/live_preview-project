package com.github.loremigliore.livepreview.services

import com.github.loremigliore.livepreview.domain.BuildTool
import com.github.loremigliore.livepreview.domain.EnvironmentState
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

@Service(Service.Level.PROJECT)
class EnvironmentStateHolder(
    private val project: Project,
    val cs: CoroutineScope,
) : Disposable {
    private val _state = MutableStateFlow(EnvironmentState(BuildTool.UNKNOWN, ""))
    val state: StateFlow<EnvironmentState> = _state.asStateFlow()

    private fun updateState(update: EnvironmentState.() -> EnvironmentState) {
        _state.value = _state.value.update()
    }

    private fun resolveBuildTool() {
        val projectDir = project.guessProjectDir()

        updateState {
            copy(
                buildTool =
                    when {
                        projectDir == null -> BuildTool.UNKNOWN

                        projectDir.findChild("amper") != null ||
                            projectDir.findChild("amper.bat") != null -> BuildTool.AMPER

                        projectDir.findChild("gradlew") != null ||
                            projectDir.findChild("build.gradle.kts") != null ||
                            projectDir.findChild("build.gradle") != null -> BuildTool.GRADLE

                        else -> BuildTool.UNKNOWN
                    },
            )
        }
    }

    private fun resolveLibrary() {
        val tempDir = System.getProperty("java.io.tmpdir")
        val osName = System.getProperty("os.name").lowercase()

        val libName =
            when {
                osName.contains("mac") -> "libasyncProfiler.dylib"
                osName.contains("linux") -> "libasyncProfiler.so"
                else -> throw IllegalStateException("Unsupported OS for Hot Reload")
            }

        val targetFile = File(tempDir, libName)

        if (!targetFile.exists()) {
            val resourceStream =
                this::class.java.getResourceAsStream("/binaries/$libName")
                    ?: throw IllegalStateException("Profiler binary not found in plugin resources")

            targetFile.outputStream().use { resourceStream.copyTo(it) }
            targetFile.setExecutable(true)
        }
        updateState {
            copy(
                library = targetFile.absolutePath.replace("\\", "/"),
            )
        }
    }

    fun setup() {
        resolveBuildTool()
        resolveLibrary()
    }

    override fun dispose() {
        cs.cancel()
    }
}
