package io.github.droidkaigi.confsched.feature.debug

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import io.github.droidkaigi.confsched.core.common.ActionEffect
import io.github.droidkaigi.confsched.core.common.ScreenChannel
import soil.query.compose.rememberMutation

context(presenterContext: DebugPresenterContext)
@Composable
fun debugScreenPresenter(
    screenChannel: ScreenChannel<DebugScreenAction, DebugScreenActionResult>,
): DebugScreenUiState {
    val soilErrorOverlayMutation = rememberMutation(presenterContext.soilErrorOverlayEnabledMutationKey)
    var dataCleared by retain { mutableStateOf(false) }
    val soilErrorOverlayEnabled by presenterContext.debugPreferencesStore
        .soilErrorOverlayEnabled.collectAsState(initial = true)
    val soilErrors by presenterContext.soilErrorMonitor.errors.collectAsState()

    ActionEffect(screenChannel) { action ->
        when (action) {
            DebugScreenAction.ClearData -> {
                presenterContext.persistedDataResetter.clearAll()
                dataCleared = true
            }
            is DebugScreenAction.SetSoilErrorOverlayEnabled ->
                soilErrorOverlayMutation.mutateAsync(action.enabled)
        }
    }

    return DebugScreenUiState(
        appVersion = presenterContext.buildConfig.versionName,
        dataCleared = dataCleared,
        soilErrorOverlayEnabled = soilErrorOverlayEnabled,
        soilErrors = soilErrors,
    )
}
