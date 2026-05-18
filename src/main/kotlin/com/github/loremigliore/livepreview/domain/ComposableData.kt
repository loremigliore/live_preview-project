package com.github.loremigliore.livepreview.domain

data class ComposableData(
    val fqName: String,
    val name: String,
    val parameters: List<ComposableParameter>,
    val code: String,
)
