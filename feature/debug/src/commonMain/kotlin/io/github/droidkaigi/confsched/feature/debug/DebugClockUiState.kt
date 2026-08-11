package io.github.droidkaigi.confsched.feature.debug

data class DebugClockUiState(
    val now: String,
    val offsetLabel: String,
    val shifted: Boolean,
    val overlayEnabled: Boolean,
    val invalidInput: Boolean,
) {
    companion object
}

internal fun DebugClockUiState.Companion.fake(): DebugClockUiState = DebugClockUiState(
    now = "2026-09-02 10:00:00 JST",
    offsetLabel = "+2h 15m",
    shifted = true,
    overlayEnabled = true,
    invalidInput = false,
)
