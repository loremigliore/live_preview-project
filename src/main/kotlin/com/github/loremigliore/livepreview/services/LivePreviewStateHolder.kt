package com.github.loremigliore.livepreview.services

import com.github.loremigliore.livepreview.domain.LivePreviewState
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Service(Service.Level.PROJECT)
class LivePreviewStateHolder(
    project: Project,
    val cs: CoroutineScope,
) : Disposable {
    private val _state = MutableStateFlow(LivePreviewState())
    val state: StateFlow<LivePreviewState> = _state.asStateFlow()

    private fun updateState(update: LivePreviewState.() -> LivePreviewState) {
        _state.value = _state.value.update()
    }

    fun updateComposables(livePreviewState: LivePreviewState) = updateState { livePreviewState }

    fun reset() = updateState { copy(composables = emptyList()) }

    override fun dispose() {
        cs.cancel()
    }
}
