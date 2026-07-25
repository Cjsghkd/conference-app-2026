package io.github.droidkaigi.confsched.feature.debug

import soil.query.core.ErrorRecord

data class DebugScreenUiState(
    val appVersion: String,
    val dataCleared: Boolean,
    val soilErrorOverlayEnabled: Boolean,
    val soilErrors: List<ErrorRecord>,
)
