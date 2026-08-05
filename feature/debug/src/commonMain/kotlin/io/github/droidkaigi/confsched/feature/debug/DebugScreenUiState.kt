package io.github.droidkaigi.confsched.feature.debug

data class DebugScreenUiState(
    val appVersion: String,
    val dataCleared: Boolean,
    val soilErrorOverlayEnabled: Boolean,
    val soilErrors: List<SoilError>,
    val clock: DebugClockUiState,
)

data class DebugClockUiState(
    val now: String,
    val offsetLabel: String,
    val shifted: Boolean,
    val invalidInput: Boolean,
)

internal fun previewDebugClockUiState() = DebugClockUiState(
    now = "2026-09-02 10:00:00 JST",
    offsetLabel = "+2h 15m",
    shifted = true,
    invalidInput = false,
)
