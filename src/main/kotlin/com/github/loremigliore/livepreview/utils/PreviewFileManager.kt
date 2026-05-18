package com.github.loremigliore.livepreview.utils

import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import java.io.File

object PreviewFileManager {
    private fun getFile(
        project: Project,
        fqName: String,
    ): File {
        val baseDir = File(project.basePath, ".livepreview")
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }

        val safeFileName = "${fqName.replace(".", "_")}.txt"
        return File(baseDir, safeFileName)
    }

    fun readPreviewCode(
        project: Project,
        fqName: String,
    ): String? {
        val file = getFile(project, fqName)
        return if (file.exists()) file.readText() else null
    }

    fun writePreviewCode(
        project: Project,
        fqName: String,
        code: String,
    ) {
        val file = getFile(project, fqName)
        file.writeText(code)
    }

    fun deletePreviewCode(
        project: Project,
        fqName: String,
    ) {
        val file = getFile(project, fqName)
        if (file.exists()) {
            file.delete()
        }
    }

    fun getCurrentComposables(ktFile: KtFile): List<KtNamedFunction> {
        val allFunctions = PsiTreeUtil.collectElementsOfType(ktFile, KtNamedFunction::class.java)
        return allFunctions.filter { ktFunction ->
            ktFunction.annotationEntries.any { it.shortName?.asString() == "Composable" }
        }
    }
}
