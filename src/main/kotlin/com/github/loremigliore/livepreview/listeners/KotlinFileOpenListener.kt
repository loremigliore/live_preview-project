package com.github.loremigliore.livepreview.listeners

import com.github.loremigliore.livepreview.logic.processFile
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class KotlinFileOpenListener(
    private val project: Project,
) : FileEditorManagerListener {
    override fun fileOpened(
        source: FileEditorManager,
        file: VirtualFile,
    ) {
        processFile(project, file)
    }

    override fun selectionChanged(event: FileEditorManagerEvent) {
        val newFile = event.newFile ?: return
        processFile(project, newFile)
    }
}
