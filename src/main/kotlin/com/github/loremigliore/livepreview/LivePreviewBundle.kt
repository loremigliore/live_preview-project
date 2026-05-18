package com.github.loremigliore.livepreview

import com.intellij.DynamicBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val BUNDLE = "messages.LivePreviewBundle"

object LivePreviewBundle : DynamicBundle(BUNDLE) {
    @JvmStatic
    fun message(
        @PropertyKey(resourceBundle = BUNDLE) key: String,
        vararg params: Any,
    ) = getMessage(key, *params)
}
