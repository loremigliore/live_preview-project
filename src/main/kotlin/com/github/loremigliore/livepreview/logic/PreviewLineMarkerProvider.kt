package com.github.loremigliore.livepreview.logic

import com.github.loremigliore.livepreview.LivePreviewBundle
import com.github.loremigliore.livepreview.icons.PluginIcons
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtNamedFunction

class PreviewLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element.node.elementType != KtTokens.IDENTIFIER) return null
        val ktFunction = element.parent as? KtNamedFunction ?: return null

        val isComposable =
            ktFunction.annotationEntries.any {
                it.shortName?.asString() == "Composable"
            }
        if (!isComposable) return null

        return LineMarkerInfo(
            element,
            element.textRange,
            PluginIcons.RunPreview,
            { LivePreviewBundle.message("run.button.tooltip") },
            createNavigationHandler(ktFunction),
            GutterIconRenderer.Alignment.LEFT,
            { LivePreviewBundle.message("run.button.tooltip.accessibility") },
        )
    }

    private fun createNavigationHandler(ktFunction: KtNamedFunction) =
        GutterIconNavigationHandler<PsiElement> { _, _ ->
            val functionName = ktFunction.name ?: return@GutterIconNavigationHandler
            val fqName = ktFunction.fqName?.asString() ?: return@GutterIconNavigationHandler

            RunPreview.launchSafe(
                project = ktFunction.project,
                functionName = functionName,
                fqName = fqName,
                psiFile = ktFunction.containingFile,
            )
        }
}
