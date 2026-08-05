package io.github.droidkaigi.confsched.feature.debug

import androidx.compose.runtime.Composable
import io.github.droidkaigi.confsched.core.common.context
import io.github.droidkaigi.confsched.core.common.retainScreenChannel

@Composable
context(screenContext: DebugScreenContext)
fun DebugScreenRoot(
    onNavigateToSoilErrors: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val screenChannel = retainScreenChannel<DebugScreenAction, DebugScreenActionResult>()

    val uiState = context(screenContext.presenterContext) {
        debugScreenPresenter(screenChannel = screenChannel)
    }

    DebugScreen(
        uiState = uiState,
        toggleSoilErrorOverlay = { enabled ->
            screenChannel.send(DebugScreenAction.SetSoilErrorOverlayEnabled(enabled))
        },
        applyClockPreset = { preset ->
            screenChannel.send(DebugScreenAction.ApplyClockPreset(preset))
        },
        shiftClockTo = { isoInstant ->
            screenChannel.send(DebugScreenAction.ShiftClockTo(isoInstant))
        },
        resetClock = { screenChannel.send(DebugScreenAction.ResetClock) },
        onOpenSoilErrors = onNavigateToSoilErrors,
        onClearData = { screenChannel.send(DebugScreenAction.ClearData) },
        onBack = onNavigateBack,
    )
}
