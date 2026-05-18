package com.github.loremigliore.livepreview.services

import com.github.loremigliore.livepreview.listeners.ComposableSignatureListener
import com.github.loremigliore.livepreview.logic.processFile
import com.github.loremigliore.livepreview.theme.LivePreviewTheme
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Service(Service.Level.PROJECT)
class SignatureListenerService(
    private val project: Project,
    val cs: CoroutineScope,
) : Disposable {
    private var debounceJob: Job? = null
    private var activeListener: ComposableSignatureListener? = null

    fun startWatching(file: VirtualFile) {
        stopWatching()

        val listener = ComposableSignatureListener(project, file)
        activeListener = listener
        PsiManager.getInstance(project).addPsiTreeChangeListener(listener, this)
    }

    fun stopWatching() {
        activeListener?.let {
            PsiManager.getInstance(project).removePsiTreeChangeListener(it)
            activeListener = null
        }
        debounceJob?.cancel()
    }

    fun scan(file: VirtualFile) {
        debounceJob?.cancel()
        debounceJob =
            cs.launch {
                delay(LivePreviewTheme.DEBOUNCE_DURATION.milliseconds)
                processFile(project, file)
            }
    }

    override fun dispose() {
        cs.cancel()
    }
}
