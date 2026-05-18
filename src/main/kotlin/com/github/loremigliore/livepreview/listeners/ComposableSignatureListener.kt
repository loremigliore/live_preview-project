package com.github.loremigliore.livepreview.listeners

import com.github.loremigliore.livepreview.utils.signatureListenerService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiTreeChangeAdapter
import com.intellij.psi.PsiTreeChangeEvent
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtNamedFunction

class ComposableSignatureListener(
    private val project: Project,
    private val targetFile: VirtualFile,
) : PsiTreeChangeAdapter() {
    override fun childrenChanged(event: PsiTreeChangeEvent) {
        handlePsiChange(event)
    }

    override fun childReplaced(event: PsiTreeChangeEvent) {
        handlePsiChange(event)
    }

    private fun handlePsiChange(event: PsiTreeChangeEvent) {
        if (event.file?.virtualFile != targetFile) return

        val element = event.parent ?: event.child ?: return
        val ktFunction = PsiTreeUtil.getParentOfType(element, KtNamedFunction::class.java, false) ?: return

        val isComposable = ktFunction.annotationEntries.any { it.shortName?.asString() == "Composable" }
        if (!isComposable) return

        val body = ktFunction.bodyBlockExpression ?: ktFunction.bodyExpression
        if (body != null && PsiTreeUtil.isAncestor(body, element, false)) {
            return
        }

        project.signatureListenerService.scan(targetFile)
    }
}
