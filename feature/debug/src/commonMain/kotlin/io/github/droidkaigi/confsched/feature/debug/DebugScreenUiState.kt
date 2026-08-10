package io.github.droidkaigi.confsched.feature.debug

data class DebugScreenUiState(
    val appVersion: String,
    val dataCleared: Boolean,
    val soilErrorOverlayEnabled: Boolean,
    val soilErrors: List<SoilError>,
    val clock: DebugClockUiState,
)
