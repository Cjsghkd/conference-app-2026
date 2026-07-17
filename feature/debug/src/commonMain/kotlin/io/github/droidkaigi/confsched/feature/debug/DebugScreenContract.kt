package io.github.droidkaigi.confsched.feature.debug

import soil.query.core.ErrorRecord

sealed interface DebugScreenAction {
    data object ClearData : DebugScreenAction
    data class SetSoilErrorOverlayEnabled(val enabled: Boolean) : DebugScreenAction
}

sealed interface DebugScreenActionResult

data class DebugScreenUiState(
    val appVersion: String,
    val dataCleared: Boolean,
    val soilErrorOverlayEnabled: Boolean,
    val soilErrors: List<ErrorRecord>,
)
