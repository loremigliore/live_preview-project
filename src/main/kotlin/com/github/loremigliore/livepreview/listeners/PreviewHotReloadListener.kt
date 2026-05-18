package com.github.loremigliore.livepreview.listeners

import com.github.loremigliore.livepreview.logic.OrchestratorClient
import com.intellij.openapi.compiler.CompilerManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.ProjectManager

class PreviewHotReloadListener : FileDocumentManagerListener {
    override fun beforeDocumentSaving(document: Document) {
        val file = FileDocumentManager.getInstance().getFile(document) ?: return
        if (file.extension != "kt") return

        ProjectManager.getInstance().openProjects.forEach { project ->
            // 1. Resolve the module context of the modified file
            val module = ModuleUtilCore.findModuleForFile(file, project) ?: return@forEach
            val compilerManager = CompilerManager.getInstance(project)

            // 2. Trigger an incremental make on the module.
            // This safely invokes the Compose Compiler plugin.
            compilerManager.make(module) { aborted, errors, _, _ ->
                if (!aborted && errors == 0) {
                    OrchestratorClient.sendHotReloadPayload(project, file.path)
                }
            }
        }
    }
}
