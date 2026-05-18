package com.github.loremigliore.livepreview.startup
import com.github.loremigliore.livepreview.logic.processFile
import com.github.loremigliore.livepreview.utils.environmentStateHolder
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.startup.ProjectActivity
import java.io.File

class LivePreviewStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        val basePath = project.guessProjectDir()?.path ?: return
        val tempDir = File(basePath, "build/hot-reload-previews")
        if (tempDir.exists()) {
            tempDir.deleteRecursively()
        }

        val currentFile = FileEditorManager.getInstance(project).selectedFiles.firstOrNull()
        if (currentFile != null) {
            processFile(project, currentFile)
        }

        project.environmentStateHolder.setup()
    }
}
