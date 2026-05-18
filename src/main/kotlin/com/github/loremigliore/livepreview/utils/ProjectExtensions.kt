package com.github.loremigliore.livepreview.utils

import com.github.loremigliore.livepreview.services.EnvironmentStateHolder
import com.github.loremigliore.livepreview.services.LivePreviewStateHolder
import com.github.loremigliore.livepreview.services.SignatureListenerService
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import java.awt.Component

fun Component.requireProject(): Project =
    DataManager
        .getInstance()
        .getDataContext(this)
        .getData(CommonDataKeys.PROJECT)
        ?: error("Project not found in DataContext. Component must be attached to a tool window.")

val Project.livePreviewStateHolder: LivePreviewStateHolder get() = getService(LivePreviewStateHolder::class.java)
val Project.signatureListenerService: SignatureListenerService get() = getService(SignatureListenerService::class.java)
val Project.environmentStateHolder: EnvironmentStateHolder get() = getService(EnvironmentStateHolder::class.java)
